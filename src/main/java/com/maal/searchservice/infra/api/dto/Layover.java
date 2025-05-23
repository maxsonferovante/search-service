package com.maal.searchservice.infra.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Layover {
    private Integer duration;
    private String name;
    private String id;
    private Boolean overnight;

}