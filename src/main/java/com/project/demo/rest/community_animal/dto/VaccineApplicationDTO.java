package com.project.demo.rest.community_animal.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class VaccineApplicationDTO {
    private Long vaccineId;
    private LocalDate applicationDate;
}
