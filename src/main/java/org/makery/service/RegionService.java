package org.makery.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.makery.dto.RegionResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegionService {

    private final ObjectMapper objectMapper;
    private List<RegionResponse> cachedRegions = new ArrayList<>();

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("regions.json");
            try (InputStream inputStream = resource.getInputStream()) {
                cachedRegions = objectMapper.readValue(inputStream, new TypeReference<List<RegionResponse>>() {});
                log.info("Successfully loaded {} regions from regions.json", cachedRegions.size());
            }
        } catch (Exception e) {
            log.error("Failed to load regions.json", e);
        }
    }

    public List<RegionResponse> getAllRegions() {
        return cachedRegions;
    }
}
