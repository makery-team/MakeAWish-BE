package org.makery.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class OrderSchemaUtil {

    public static Map<String, Object> createDefaultOrderSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("size", Map.of("type", "string", "label", "케이크 사이즈 (도시락, 미니, 1호, 2호, 3호)"));
        properties.put("flavor", Map.of("type", "string", "label", "시트 및 크림 맛 (바닐라생크림, 초코가나슈, 오레오, 얼그레이)"));
        properties.put("lettering", Map.of("type", "string", "label", "레터링 문구 (케이크 위 및 케이크 판 문구, 20자 이내)"));
        properties.put("pickupDate", Map.of("type", "string", "label", "픽업 희망 일시 (날짜 및 시간)"));
        properties.put("request", Map.of("type", "string", "label", "알러지 및 추가 요청사항"));

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        return schema;
    }
}
