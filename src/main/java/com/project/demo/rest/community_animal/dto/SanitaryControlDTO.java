package com.project.demo.rest.community_animal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SanitaryControlDTO {
    private LocalDate lastApplicationDate;
    private String productUsed;
    private Long sanitaryControlTypeId;
    private Long sanitaryControlResponseId;
}
