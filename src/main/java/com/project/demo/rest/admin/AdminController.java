package com.project.demo.rest.admin;

import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.interest.Interest;
import com.project.demo.logic.entity.interest.InterestRepository;
import com.project.demo.logic.entity.municipality.Municipality;
import com.project.demo.logic.entity.municipality.MunicipalityRepository;
import com.project.demo.logic.entity.neighborhood.Neighborhood;
import com.project.demo.logic.entity.neighborhood.NeighborhoodRepository;
import com.project.demo.logic.entity.rol.Role;
import com.project.demo.logic.entity.rol.RoleEnum;
import com.project.demo.logic.entity.rol.RoleRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.rest.auth.dto.RegisterUserRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RequestMapping("/admin")
@RestController
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private NeighborhoodRepository neighborhoodRepository;

    @Autowired
    private MunicipalityRepository municipalityRepository;

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody RegisterUserRequestDTO dto, HttpServletRequest request) {
        if (dto.getName() == null || dto.getLastname() == null || dto.getEmail() == null) {
            return new GlobalResponseHandler().handleResponse(
                    "Missing required fields",
                    null, HttpStatus.BAD_REQUEST, request);
        }

        if (dto.getPassword() == null) {
            dto.setPassword(UUID.randomUUID().toString().substring(0, 8));
        }

        Municipality municipality = dto.getMunicipalityId() != null
                ? municipalityRepository.findById(dto.getMunicipalityId()).orElse(null) : null;

        Neighborhood neighborhood = dto.getNeighborhoodId() != null
                ? neighborhoodRepository.findById(dto.getNeighborhoodId()).orElse(null) : null;

        Set<Interest> interests = dto.getInterestIds() != null
                ? new HashSet<>(interestRepository.findAllById(dto.getInterestIds())) : new HashSet<>();

        Set<Role> roles = dto.getRoleIds() != null
                ? new HashSet<>(roleRepository.findAllById(dto.getRoleIds())) : new HashSet<>();

        User user = User.builder()
                .name(dto.getName())
                .lastname(dto.getLastname())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phoneNumber(dto.getPhoneNumber())
                .identificationCard(dto.getIdentificationCard())
                .birthDate(dto.getBirthDate())
                .municipality(municipality)
                .neighborhood(neighborhood)
                .interests(interests)
                .roles(roles)
                .nurseryHome(false)
                .requiresPasswordChange(true)
                .active(true)
                .registeredByCensusTaker(false)
                .socialLoginCompleted(false)
                .usedSocialLogin(false)
                .build();

        userRepository.save(user);

        return new GlobalResponseHandler().handleResponse(
                "User created successfully",
                user, HttpStatus.CREATED, request);
    }
}
