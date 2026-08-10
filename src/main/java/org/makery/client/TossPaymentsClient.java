package org.makery.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "tossPaymentsClient", url = "https://api.tosspayments.com")
public interface TossPaymentsClient {

    @PostMapping("/v1/payments/confirm")
    Object confirmPayment(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> requestBody
    );
}