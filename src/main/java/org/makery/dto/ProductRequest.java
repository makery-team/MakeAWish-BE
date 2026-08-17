package org.makery.dto;

public record ProductRequest(
    String name,
    Integer price,
    String description
) {}
