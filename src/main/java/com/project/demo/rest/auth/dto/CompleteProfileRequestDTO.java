package com.project.demo.rest.auth.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

/**
 * DTO for completing user profile information.
 * This class is used to transfer data related to user profile completion.
 * It includes fields such as identification card, phone number, birth date,
 * nursery home status, neighborhood and municipality IDs, interests, and volunteer status.
 * @author dgutierrez
 */
@Getter
@Setter
public class CompleteProfileRequestDTO {
    private String identificationCard;

    private String phoneNumber;

    private LocalDate birthDate;

    private boolean nurseryHome;

    private Long neighborhoodId;

    private Long municipalityId;

    private Set<Long> interestIds;

    private boolean wantsToBeVolunteer;
}
