package com.example.quanly;

import com.example.quanly.service.UploadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UploadServiceTest {
    @TempDir
    Path tempDirectory;

    @Test
    void storesValidatedImageWithServerGeneratedName() throws Exception {
        UploadService service = new UploadService(tempDirectory.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file", "../../payload.png", "image/png", validPng());

        String storedName = service.handleSaveUploadFile(file, "avatar");

        assertTrue(storedName.matches("[0-9a-f-]{36}\\.png"));
        assertFalse(storedName.contains("payload"));
        assertTrue(Files.exists(tempDirectory.resolve("avatar").resolve(storedName)));
    }

    @Test
    void rejectsSpoofedImageContent() {
        UploadService service = new UploadService(tempDirectory.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file", "attack.png", "image/png", "<script>alert(1)</script>".getBytes());

        assertThrows(IllegalArgumentException.class,
                () -> service.handleSaveUploadFile(file, "avatar"));
    }

    @Test
    void rejectsUnknownTargetFolder() throws Exception {
        UploadService service = new UploadService(tempDirectory.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file", "image.png", "image/png", validPng());

        assertThrows(IllegalArgumentException.class,
                () -> service.handleSaveUploadFile(file, "../outside"));
    }

    private byte[] validPng() throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}
