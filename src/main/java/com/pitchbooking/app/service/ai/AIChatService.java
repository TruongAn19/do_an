package com.pitchbooking.app.service.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@ConditionalOnProperty(name = "ai.chat.enabled", havingValue = "true")
public class AIChatService {

    private final ChatClient aiClient;
    private final AIToolsConfig aiToolsConfig;

    public AIChatService(
            @Qualifier("openAiChatModel") ChatModel chatModel,
            AIToolsConfig aiToolsConfig) {
        
        this.aiToolsConfig = aiToolsConfig;

        String systemPrompt = """
                Bạn là trợ lý ảo thông minh của Hệ thống Đặt Sân Bóng Đá.
                Nhiệm vụ của bạn:
                1. Hỗ trợ khách hàng đặt sân bằng cách dùng công cụ 'listAllPitches' để tìm sân/chi nhánh phù hợp và 'checkPitchAvailability' để kiểm tra lịch trống.
                2. Trả lời các câu hỏi về địa chỉ, giá cả, dịch vụ dựa trên thông tin thực tế từ hệ thống.
                3. Tư vấn loại sân bóng đá (5 người, 7 người), cỏ nhân tạo vs cỏ tự nhiên, các loại giày đinh phù hợp với mặt sân.
                Phong cách trả lời: Thân thiện, chuyên nghiệp, ngắn gọn.
                QUY TẮC QUAN TRỌNG:
                - KHÔNG ĐƯỢC tự ý trả lời là hệ thống không hỗ trợ một khu vực nào (ví dụ: Hà Nội) khi chưa gọi công cụ 'listAllPitches' để kiểm tra.
                - Nếu khách hỏi về địa điểm hoặc muốn tìm sân ở một khu vực, hãy gọi 'listAllPitches' trước.
                - Luôn sử dụng dữ liệu thực tế từ công cụ, không dùng thông tin cũ hoặc giả định.
                """;

        var chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(10)
                .build();

        this.aiClient = ChatClient.builder(chatModel)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultToolCallbacks(org.springframework.ai.support.ToolCallbacks.from(aiToolsConfig))
                .build();
    }

    public Flux<String> chatStream(String chatId, String message) {
        return aiClient.prompt()
                .user(message)
                .advisors(conf -> conf.param("chat_memory_conversation_id", chatId))
                .stream()
                .content();
    }

    public String chat(String chatId, String message) {
        return aiClient.prompt()
                .user(message)
                .advisors(conf -> conf.param("chat_memory_conversation_id", chatId))
                .call()
                .content();
    }
}
