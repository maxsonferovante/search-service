package com.maal.searchservice.infra.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CarbonEmissions {
    private Integer thisFlight;
    private Integer typicalForThisRoute;
    private Integer differencePercent;
}