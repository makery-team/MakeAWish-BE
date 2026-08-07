package org.makery.controller;

import lombok.RequiredArgsConstructor;
import org.makery.dto.ImageUploadResponse;
import org.makery.websocket.AwsS3Service; // 💡 AwsS3Service로 올바르게 Import
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageUploadController {

    // 💡 S3UploadService 대신 실제 구현체인 AwsS3Service 주입
    private final AwsS3Service awsS3Service;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file
    ) {
        // 💡 awsS3Service.uploadFile() 호출
        String imageUrl = awsS3Service.uploadFile(file);
        return ResponseEntity.ok(new ImageUploadResponse(imageUrl));
    }
}