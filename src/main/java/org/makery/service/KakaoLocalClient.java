package org.makery.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;

@FeignClient(name = "kakao-local-client", url = "https://dapi.kakao.com")
public interface KakaoLocalClient {

    @GetMapping("/v2/local/search/address.json")
    Map<String, Object> searchAddress(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("query") String query
    );
}
