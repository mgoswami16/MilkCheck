package com.milkcheck.service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp");

    private final Path productDir;
    private final long maxBytes;

    public FileStorageService(
            @Value("${app.upload.dir:uploads}") String baseDir,
            @Value("${app.upload.max-bytes:5242880}") long maxBytes) {
        this.productDir = Paths.get(baseDir, "products").toAbsolutePath().normalize();
        this.maxBytes = maxBytes;
    }

    @PostConstruct
    void init() throws IOException {
        Files.createDirectories(productDir);
        log.info("Product image directory: {}", productDir);
    }

    /** Stores a product image and returns its public URL path. */
    public String storeProductImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No image file selected");
        }
        if (file.getSize() > maxBytes) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image must be 5 MB or smaller");
        }
        String extension = EXTENSIONS.get(file.getContentType());
        if (extension == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only JPG, PNG or WEBP images are allowed");
        }

        String fileName = UUID.randomUUID() + extension;
        Path target = productDir.resolve(fileName).normalize();

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to store product image {}", fileName, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save image");
        }

        log.info("Stored product image {} ({} bytes)", fileName, file.getSize());
        return "/uploads/products/" + fileName;
    }
}