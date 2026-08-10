package org.makery.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StoreProfileUpdateRequest {

    @NotBlank(message = "매장 이름은 필수 항목입니다.")
    private String name;

    private String description;
    private String address;
    private String phone;
    private String hours;
    private String notice;
    private String cautionNotice; // 알러지 및 주의사항 안내문
    private String keywords; // 매장 핵심 키워드

    private Double latitude;
    private Double longitude;
}
