package com.project.demo.rest.municipality.dto;

import com.project.demo.logic.entity.municipality.MunicipalityStatusEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMunicipalityRequestDTO {
    private String name;
    private String address;
    private String phone;
    private String email;
    private Long cantonId;
    private String responsibleName;
    private String responsiblePosition;
    private MunicipalityStatusEnum status;
}
