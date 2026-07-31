package com.example.quanly.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Chat;
import com.google.genai.Client;
import com.google.genai.errors.ClientException;
import com.google.genai.types.Content;
import com.google.genai.types.FunctionCall;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.google.genai.types.Tool;
import com.example.quanly.exception.AIProviderException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ConditionalOnProperty(name = "ai.chat.enabled", havingValue = "true")
public class AIChatService {

        private static final int MAX_TOOL_ROUNDS = 5;
        private static final String SYSTEM_PROMPT_TEMPLATE = """
                        Bạn là trợ lý ảo thông minh của hệ thống đặt sân và thuê vợt Pickleball.
                        Ngày hiện tại của hệ thống là %s.
                        Hãy trả lời thân thiện, chuyên nghiệp và ngắn gọn.
                        Khi người dùng hỏi địa điểm, sân hoặc chi nhánh, luôn gọi listAllCourts trước.
                        Khi người dùng hỏi lịch trống, gọi checkCourtAvailability với ngày YYYY-MM-DD.
                        Chỉ dùng dữ liệu thực tế từ công cụ, không tự đoán địa chỉ, giá hoặc lịch trống.
                        Các công cụ chỉ dùng để tra cứu. Không được tuyên bố đã đặt sân, giữ chỗ, hủy đơn hoặc thanh toán cho người dùng.
                        Báo cáo doanh thu chỉ dành cho quản trị viên và phải dùng getRevenueReport.
                        Nếu người dùng chỉ nói tháng mà không nói năm, phải dùng năm của ngày hiện tại.
                        Không được tự chọn một năm cũ. Khi báo cáo, phải giữ nguyên khoảng ngày công cụ trả về.
                        Bạn cũng có thể tư vấn kỹ thuật Pickleball và lựa chọn vợt Pickleball.
                        """;

        private final Client client;
        private final String model;
        private final AIToolsConfig tools;
        private final ObjectMapper objectMapper;
        private final GenerateContentConfig chatConfig;
        private final Map<String, Chat> conversations = new ConcurrentHashMap<>();

        public AIChatService(@Value("${gemini.api-key}") String apiKey,
                        @Value("${gemini.model:gemini-flash-latest}") String model,
                        AIToolsConfig tools,
                        ObjectMapper objectMapper) {
                if (apiKey == null || apiKey.isBlank() || apiKey.contains("placeholder")) {
                        throw new IllegalStateException("GEMINI_API_KEY chưa được cấu hình");
                }
                this.client = Client.builder().apiKey(apiKey).build();
                this.model = model;
                this.tools = tools;
                this.objectMapper = objectMapper;
                this.chatConfig = GenerateContentConfig.builder()
                                .systemInstruction(Content.fromParts(Part.fromText(
                                                SYSTEM_PROMPT_TEMPLATE.formatted(LocalDate.now()))))
                                .tools(buildTools())
                                .build();
        }

        public Flux<String> chatStream(String chatId, String message) {
                CallerContext caller = currentCaller();
                return Flux.defer(() -> Flux.just(chat(chatId, message, caller)));
        }

        public String chat(String chatId, String message) {
                return chat(chatId, message, currentCaller());
        }

        private String chat(String chatId, String message, CallerContext caller) {
                if (message == null || message.isBlank()) {
                        throw new IllegalArgumentException("Nội dung câu hỏi không được để trống");
                }
                if (message.length() > 4_000) {
                        throw new IllegalArgumentException("Nội dung câu hỏi không được vượt quá 4000 ký tự");
                }
                String clientChatId = chatId == null || chatId.isBlank() ? "default-session" : chatId.trim();
                if (clientChatId.length() > 100) {
                        throw new IllegalArgumentException("Mã phiên chat không hợp lệ");
                }
                String conversationId = caller.identity() + ":" + clientChatId;
                Chat chat = conversations.computeIfAbsent(conversationId,
                                ignored -> client.chats.create(model, chatConfig));

                synchronized (chat) {
                        try {
                                String datedMessage = "[Ngày hệ thống hiện tại: " + LocalDate.now() + "]\n" + message;
                                GenerateContentResponse response = chat.sendMessage(datedMessage);
                                for (int round = 0; round < MAX_TOOL_ROUNDS; round++) {
                                        List<FunctionCall> calls = response.functionCalls();
                                        if (calls == null || calls.isEmpty()) {
                                                String text = response.text();
                                                return text == null || text.isBlank()
                                                                ? "Xin lỗi, tôi chưa thể tạo câu trả lời lúc này."
                                                                : text;
                                        }

                                        List<Part> results = new ArrayList<>();
                                        for (FunctionCall call : calls) {
                                                String name = call.name().orElseThrow(() -> new IllegalStateException(
                                                                "Gemini trả về function call không có tên"));
                                                results.add(Part.fromFunctionResponse(name,
                                                                executeTool(name, call.args().orElse(Map.of()),
                                                                                caller.admin())));
                                        }
                                        response = chat.sendMessage(Content.fromParts(results.toArray(Part[]::new)));
                                }
                                throw new IllegalStateException("Gemini gọi công cụ quá nhiều lần liên tiếp");
                        } catch (ClientException ex) {
                                conversations.remove(conversationId, chat);
                                throw new AIProviderException(
                                                "Gemini từ chối yêu cầu. Hãy kiểm tra GEMINI_API_KEY và quyền truy cập Gemini API.",
                                                ex);
                        }
                }
        }

        private Map<String, Object> executeTool(String name, Map<String, Object> args, boolean admin) {
                Object result = switch (name) {
                        case "listAllCourts" -> tools.listAllCourts();
                        case "checkCourtAvailability" -> tools.checkCourtAvailability(
                                        objectMapper.convertValue(args, AIToolsConfig.CourtAvailabilityRequest.class));
                        case "getRevenueReport" -> tools.getRevenueReport(
                                        objectMapper.convertValue(args, AIToolsConfig.RevenueRequest.class), admin);
                        default -> throw new IllegalArgumentException("Công cụ AI không được hỗ trợ: " + name);
                };
                return objectMapper.convertValue(result, objectMapper.getTypeFactory()
                                .constructMapType(Map.class, String.class, Object.class));
        }

        private Tool buildTools() {
                FunctionDeclaration listCourts = FunctionDeclaration.builder()
                                .name("listAllCourts")
                                .description("Liệt kê các sân Pickleball đang hoạt động cùng khu vực và địa chỉ.")
                                .parametersJsonSchema(Map.of("type", "object", "properties", Map.of()))
                                .build();
                FunctionDeclaration availability = FunctionDeclaration.builder()
                                .name("checkCourtAvailability")
                                .description("Kiểm tra lịch trống của các sân theo ngày.")
                                .parametersJsonSchema(Map.of("type", "object",
                                                "properties", Map.of("date", Map.of("type", "string",
                                                                "description", "Ngày định dạng YYYY-MM-DD")),
                                                "required", List.of("date")))
                                .build();
                FunctionDeclaration revenue = FunctionDeclaration.builder()
                                .name("getRevenueReport")
                                .description("Lấy báo cáo doanh thu trong khoảng ngày; chỉ dành cho admin. "
                                                + "Hôm nay là " + LocalDate.now()
                                                + ". Nếu người dùng không nói năm thì dùng năm hiện tại.")
                                .parametersJsonSchema(Map.of("type", "object",
                                                "properties", Map.of(
                                                                "startDate",
                                                                Map.of("type", "string", "description",
                                                                                "Ngày bắt đầu YYYY-MM-DD"),
                                                                "endDate",
                                                                Map.of("type", "string", "description",
                                                                                "Ngày kết thúc YYYY-MM-DD")),
                                                "required", List.of("startDate", "endDate")))
                                .build();
                return Tool.builder().functionDeclarations(listCourts, availability, revenue).build();
        }

        private CallerContext currentCaller() {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
                        return new CallerContext("anonymous", false);
                }
                boolean admin = auth.getAuthorities().stream()
                                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
                return new CallerContext(auth.getName(), admin);
        }

        private record CallerContext(String identity, boolean admin) {
        }
}
