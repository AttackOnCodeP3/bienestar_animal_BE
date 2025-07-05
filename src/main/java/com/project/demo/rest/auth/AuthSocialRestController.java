package com.project.demo.rest.auth;

import com.project.demo.logic.entity.interest.Interest;
import com.project.demo.logic.entity.interest.InterestRepository;
import com.project.demo.logic.entity.municipality.Municipality;
import com.project.demo.logic.entity.neighborhood.Neighborhood;
import com.project.demo.logic.entity.rol.Role;
import com.project.demo.logic.entity.rol.RoleEnum;
import com.project.demo.logic.entity.rol.RoleRepository;
import com.project.demo.logic.entity.user.LoginResponse;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.logic.entity.auth.JwtService;
import com.project.demo.rest.auth.dto.CompleteProfileRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/auth/social")
@RequiredArgsConstructor
public class AuthSocialRestController {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private InterestRepository interestRepository;

    private final UserRepository userRepository;
    private final JwtService jwtService;

    /**
     * Handles the success of a social login.
     * @param authentication the authentication object containing user details
     * @return ResponseEntity with LoginResponse containing the JWT token and user details
     * @author dgutierrez
     */
    @GetMapping("/success")
    public ResponseEntity<LoginResponse> onSocialLoginSuccess(Authentication authentication) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado después del login social."));

        String token = jwtService.generateToken(user);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setAuthUser(user);
        response.setExpiresIn(3600);

        return ResponseEntity.ok(response);
    }

    /**
     * Completes the user profile after social login.
     * @param request the request containing user details to complete the profile
     * @return ResponseEntity with LoginResponse containing the JWT token and user details
     * @author dgutierrez
     */
    @PutMapping("/complete-profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LoginResponse> completeProfile(
            @RequestBody CompleteProfileRequestDTO request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Optional<User> userOptional = userRepository.findById(currentUser.getId());
        if (userOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found.");
        }

        User user = userOptional.get();

        Optional<Role> communityRole = roleRepository.findByName(RoleEnum.COMMUNITY_USER);
        Optional<Role> volunteerRole = roleRepository.findByName(RoleEnum.VOLUNTEER_USER);

        if (communityRole.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Community role not found.");
        }

        if (request.isWantsToBeVolunteer()) {
            if (volunteerRole.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Volunteer role not found.");
            }
            if (request.getMunicipalityId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Municipality is required for volunteers.");
            }
        }

        user.setIdentificationCard(request.getIdentificationCard());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setBirthDate(request.getBirthDate());
        user.setNurseryHome(request.isNurseryHome());
        user.setNeighborhood(Neighborhood.builder().id(request.getNeighborhoodId()).build());

        if (request.getMunicipalityId() != null) {
            user.setMunicipality(Municipality.builder().id(request.getMunicipalityId()).build());
        }

        if (user.getRoles().stream().noneMatch(role -> role.getName().equals(RoleEnum.COMMUNITY_USER))) {
            user.addRole(communityRole.get());
        }

        if (request.isWantsToBeVolunteer()) {
            if (user.getRoles().stream().noneMatch(role -> role.getName().equals(RoleEnum.VOLUNTEER_USER))) {
                Role role = volunteerRole.orElseThrow(() -> new IllegalStateException("Volunteer role not available"));
                user.addRole(role);
            }
        }

        Set<Long> interestIds = request.getInterestIds();
        if (interestIds != null && !interestIds.isEmpty()) {
            Set<Interest> interests = new HashSet<>(interestRepository.findAllById(interestIds));
            if (interests.size() != interestIds.size()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more interests not found.");
            }
            user.setInterests(interests);
        }

        user.setSocialLoginCompleted(true);

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        loginResponse.setAuthUser(savedUser);
        loginResponse.setExpiresIn(jwtService.getExpirationTime());

        return ResponseEntity.ok(loginResponse);
    }
}
