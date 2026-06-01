package org.makery.domain;

public enum InpaintingStatus {
    PENDING,    // AI 서버에서 이미지 생성 중
    COMPLETED,  // 생성 완료 및 S3 업로드 완료
    FAILED      // 생성 또는 업로드 실패
}
