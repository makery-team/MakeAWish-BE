package org.makery.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderSchemaRequest {
    // 포스트맨 Body에서 {"schemaData": "..."} 로 보내는 값을 담는 변수입니다.
    private String schemaData;
}