package org.makery.controller;

import lombok.RequiredArgsConstructor;
import org.makery.dto.ImageUploadResponse;
import org.makery.service.S3UploadService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageUploadController {

    private final S3UploadService s3UploadService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file
    ) {
        String imageUrl = s3UploadService.uploadImage(file);
        return ResponseEntity.ok(new ImageUploadResponse(imageUrl));
    }
}