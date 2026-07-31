package com.example.quanly.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class UploadService {
    private static final Set<String> ALLOWED_FOLDERS = Set.of("product", "racket", "avatar");
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png");
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final int MAX_IMAGE_DIMENSION = 8_000;

    private final Path uploadRoot;

    public UploadService(@Value("${app.upload.directory:uploads/images}") String uploadDirectory) {
        this.uploadRoot = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    public String handleSaveUploadFile(MultipartFile file, String targetFolder) {
        if (file == null || file.isEmpty()) {
            return "";
        }
        if (!ALLOWED_FOLDERS.contains(targetFolder)) {
            throw new IllegalArgumentException("Thư mục tải lên không hợp lệ.");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("Ảnh tải lên không được vượt quá 5 MB.");
        }

        String extension = ALLOWED_TYPES.get(file.getContentType());
        if (extension == null) {
            throw new IllegalArgumentException("Chỉ chấp nhận ảnh JPEG hoặc PNG.");
        }

        try {
            byte[] content = file.getBytes();
            validateImageContent(content, extension);

            Path targetDirectory = uploadRoot.resolve(targetFolder).normalize();
            if (!targetDirectory.startsWith(uploadRoot)) {
                throw new IllegalArgumentException("Đường dẫn tải lên không hợp lệ.");
            }
            Files.createDirectories(targetDirectory);

            String finalName = UUID.randomUUID() + extension;
            Path temporaryFile = Files.createTempFile(targetDirectory, ".upload-", ".tmp");
            try {
                Files.write(temporaryFile, content);
                Files.move(temporaryFile, targetDirectory.resolve(finalName),
                        StandardCopyOption.ATOMIC_MOVE);
            } finally {
                Files.deleteIfExists(temporaryFile);
            }
            return finalName;
        } catch (IOException ex) {
            throw new IllegalStateException("Không thể lưu ảnh tải lên.", ex);
        }
    }

    private void validateImageContent(byte[] content, String extension) throws IOException {
        boolean validSignature = extension.equals(".png")
                ? isPng(content)
                : isJpeg(content);
        if (!validSignature) {
            throw new IllegalArgumentException("Nội dung tệp không khớp với định dạng ảnh.");
        }

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(content));
        if (image == null) {
            throw new IllegalArgumentException("Tệp tải lên không phải ảnh hợp lệ.");
        }
        if (image.getWidth() > MAX_IMAGE_DIMENSION || image.getHeight() > MAX_IMAGE_DIMENSION) {
            throw new IllegalArgumentException("Kích thước ảnh không được vượt quá 8000 x 8000 pixel.");
        }
    }

    private boolean isPng(byte[] content) {
        byte[] signature = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        if (content.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if (content[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean isJpeg(byte[] content) {
        return content.length >= 4
                && content[0] == (byte) 0xFF
                && content[1] == (byte) 0xD8
                && content[content.length - 2] == (byte) 0xFF
                && content[content.length - 1] == (byte) 0xD9;
    }
}
