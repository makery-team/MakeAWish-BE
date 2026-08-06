package org.makery.websocket;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AwsS3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;
    private final RestTemplate restTemplate;

    /**
     * AI 서버가 웹훅으로 보낸 결과물(Base64 텍스트)을 S3에 직접 업로드
     */
    public String uploadFromBase64(String base64Data) {
        if (base64Data == null || base64Data.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업로드할 이미지 데이터가 비어 있습니다.");
        }

        try {
            String pureBase64 = base64Data;
            if (base64Data.contains(",")) {
                pureBase64 = base64Data.split(",")[1];
            }

            byte[] imageBytes = Base64.getDecoder().decode(pureBase64);
            InputStream inputStream = new ByteArrayInputStream(imageBytes);

            String fileName = "inpainted/" + UUID.randomUUID().toString() + ".jpg";

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(imageBytes.length);
            metadata.setContentType("image/jpeg");

            // 💡 .withCannedAcl(...) 제거하여 S3 ACL 거부 에러 방지
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, metadata));

            return amazonS3.getUrl(bucket, fileName).toString();

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 형식의 Base64 데이터입니다.", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 파일 업로드(Base64) 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 기존 임시 이미지 URL 다운로드 후 S3 업로드 로직
     */
    public String uploadFromUrl(String imageUrl) {
        try {
            byte[] imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
            if (imageBytes == null) {
                throw new RuntimeException("이미지 다운로드에 실패했습니다.");
            }
            InputStream inputStream = new ByteArrayInputStream(imageBytes);

            String fileName = "inpainted/" + UUID.randomUUID().toString() + ".png";

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(imageBytes.length);
            metadata.setContentType("image/png");

            // 💡 .withCannedAcl(...) 제거
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, metadata));

            return amazonS3.getUrl(bucket, fileName).toString();

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 업로드 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 일반 멀티파트 파일 업로드
     */
    public String uploadFile(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업로드할 파일이 존재하지 않습니다.");
        }

        String fileName = createFileName(multipartFile.getOriginalFilename());

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(multipartFile.getSize());
        objectMetadata.setContentType(multipartFile.getContentType());

        try (InputStream inputStream = multipartFile.getInputStream()) {
            // 💡 .withCannedAcl(...) 제거
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다.", e);
        }

        return amazonS3.getUrl(bucket, fileName).toString();
    }

    public String createFileName(String fileName) {
        return UUID.randomUUID().toString().concat(getFileExtension(fileName));
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return ".png"; // 기본 확장자 지정
        }
        try {
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 형식의 파일(" + fileName + ") 입니다.");
        }
    }

    public void deleteFile(String fileUrl) {
        String fileName = extractFileNameFromUrl(fileUrl);
        if (fileName == null || fileName.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일명 추출 실패: URL이 올바르지 않습니다.");
        }

        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 파일 삭제 실패", e);
        }
    }

    private String extractFileNameFromUrl(String url) {
        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex == -1 || lastSlashIndex == url.length() - 1) {
            return null;
        }
        return url.substring(lastSlashIndex + 1);
    }
}