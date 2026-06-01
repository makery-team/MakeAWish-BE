package org.makery.dto;

public record InpaintingAiRequest(
        String prompt,
        String image_url,
        String mask_b64
) {}
