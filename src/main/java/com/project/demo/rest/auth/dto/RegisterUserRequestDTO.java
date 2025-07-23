package com.project.demo.rest.auth.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

/**
 * DTO for registering a new user.
 * This class is used to transfer data related to user registration.
 * @author dgutierrez
 */
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

    private Set<Long> roleIds;

    private boolean wantsToBeVolunteer;
}
