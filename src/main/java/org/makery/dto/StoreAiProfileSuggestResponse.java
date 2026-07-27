package org.makery.dto;

import java.util.List;

public record StoreAiProfileSuggestResponse(
        Long storeId,
        String overallFeedback,
        List<String> suggestions
) {}