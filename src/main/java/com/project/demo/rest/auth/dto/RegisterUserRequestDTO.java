package com.project.demo.rest.auth.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class RegisterUserRequestDTO {
    private String name;
    private String lastname;
    private String email;
    private String password;
    private String phoneNumber;
    private String identificationCard;
    private LocalDate birthDate;
    private Long municipalityId;
    private Long neighborhoodId;
    private Set<Long> interestIds;

    private boolean wantsToBeVolunteer;
}
