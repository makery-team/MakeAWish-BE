package org.makery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewAiSummaryResponse {

    private Long storeId;                   // 매장 ID
    private Integer totalReviewCount;       // 요약에 분석된 총 리뷰 수
    private String summary;                 // AI가 요약한 종합 한줄/세줄 평
    private List<String> positivePoints;    // 장점 키워드/요약 리스트 (예: ["디자인이 예뻐요", "친절해요"])
    private List<String> negativePoints;    // 개선점/아쉬운 점 리스트 (예: ["주차가 불편해요"])
}