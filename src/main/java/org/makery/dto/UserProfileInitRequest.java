package org.makery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.makery.domain.Language;

public record UserProfileInitRequest(
        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname,

        @NotBlank(message = "전화번호는 필수입니다.")
        String phoneNumber,

        @NotNull(message = "언어 설정은 필수입니다.")
        Language language
) {}
