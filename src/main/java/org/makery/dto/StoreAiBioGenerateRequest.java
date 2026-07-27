package org.makery.dto;

public record StoreAiBioGenerateRequest(
        String keywords,
        String concept
) {}