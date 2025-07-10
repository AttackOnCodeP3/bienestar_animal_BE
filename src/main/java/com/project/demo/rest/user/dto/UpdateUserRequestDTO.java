package com.project.demo.rest.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class UpdateUserRequestDTO {
    private Long id;

    private String identificationCard;
    private String name;
    private String lastname;
    private String email;
    private String phoneNumber;
    private LocalDate birthDate;

    private Boolean nurseryHome;
    private Boolean requiresPasswordChange;
    private Boolean active;

    private Boolean registeredByCensusTaker;
    private Boolean socialLoginCompleted;
    private Boolean usedSocialLogin;

    private Long municipalityId;
    private Long neighborhoodId;
    private Set<Long> interestIds;
    private Set<Long> roleIds;
}
