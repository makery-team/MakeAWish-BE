package org.makery.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ExtraFeeCreateRequest {

    @NotNull(message = "추가금 금액은 필수입니다.")
    @Min(value = 0, message = "추가금은 0원 이상이어야 합니다.")
    private Integer extraFee;

    private String reason; // 추가금 산정 사유 (예: "3D 입체 케이크 장식 추가", "특수 색소 사용" 등)
}
