package com.tissugest.controller;

import com.tissugest.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/uploads")
@Slf4j
public class FileUploadController {

    @Value("${app.uploads.dir:./uploads}")
    private String uploadsDir;

    @PostMapping
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) throw BusinessException.badRequest("Fichier vide");

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw BusinessException.badRequest("Seules les images sont acceptées (JPG, PNG)");
        }

        // Max 2MB
        if (file.getSize() > 2 * 1024 * 1024) {
            throw BusinessException.badRequest("Fichier trop volumineux (max 2 Mo)");
        }

        try {
            // Ensure upload dir exists (use absolute path)
            Path uploadPath = Paths.get(uploadsDir).toAbsolutePath();
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            // Generate unique filename
            String ext = "";
            String originalName = file.getOriginalFilename();
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String filename = UUID.randomUUID() + ext;

            // Save file using Files.copy (more reliable than transferTo)
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            String url = "/uploads/files/" + filename;
            log.info("File uploaded: {} -> {}", originalName, url);

            return ResponseEntity.ok(Map.of("url", url, "filename", filename));
        } catch (IOException e) {
            throw BusinessException.badRequest("Erreur upload: " + e.getMessage());
        }
    }

    /**
     * Serve uploaded files (dev only — in prod, use nginx/S3)
     */
    @GetMapping("/files/{filename}")
    public ResponseEntity<byte[]> serve(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadsDir).resolve(filename);
            if (!Files.exists(filePath)) return ResponseEntity.notFound().build();

            byte[] data = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);
            return ResponseEntity.ok()
                    .header("Content-Type", contentType != null ? contentType : "application/octet-stream")
                    .header("Cache-Control", "public, max-age=86400")
                    .body(data);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
