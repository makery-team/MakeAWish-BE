package org.makery.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionResponse {
    private String name;
    private Double latitude;
    private Double longitude;
    private List<RegionDongResponse> dongs;
}
