package org.makery.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewReplyRequest {

    @NotBlank(message = "답글 내용은 필수 입력 항목입니다.")
    private String replyContent;
}
