package com.milkcheck.controller;

import com.milkcheck.service.FileStorageService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/uploads")
public class AdminUploadController {

    private final FileStorageService storage;

    public AdminUploadController(FileStorageService storage) {
        this.storage = storage;
    }

    @PostMapping(value = "/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadProductImage(@RequestParam("file") MultipartFile file) {
        String url = storage.storeProductImage(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("url", url));
    }
}