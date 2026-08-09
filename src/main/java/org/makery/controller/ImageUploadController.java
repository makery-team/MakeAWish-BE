package org.makery.controller;

import lombok.RequiredArgsConstructor;
import org.makery.dto.ImageUploadResponse;
import org.makery.websocket.AwsS3Service;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageUploadController {

    private final AwsS3Service awsS3Service;

    /**
     * 이미지 파일 업로드
     * POST /api/images/upload
     * 이미지 파일을 S3에 업로드하고 접근 URL을 반환합니다.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file
    ) {
        String imageUrl = awsS3Service.uploadFile(file);
        return ResponseEntity.ok(new ImageUploadResponse(imageUrl));
    }
}