package com.project.demo.rest.auth;

import com.project.demo.logic.constants.general.GeneralConstants;
import com.project.demo.logic.entity.auth.AuthenticationService;
import com.project.demo.logic.entity.auth.JwtService;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
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
import com.project.demo.rest.auth.dto.RegisterUserRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RequestMapping("/auth")
@RestController
public class AuthRestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private InterestRepository interestRepository;

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;

    public AuthRestController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody User user) {
        User authenticatedUser = authenticationService.authenticate(user);

        String jwtToken = jwtService.generateToken(authenticatedUser);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(jwtToken);
        loginResponse.setExpiresIn(jwtService.getExpirationTime());

        Optional<User> foundedUser = userRepository.findByEmail(user.getEmail());

        foundedUser.ifPresent(u -> {
            u.setLastLoginDate(LocalDateTime.now());
            userRepository.save(u);
        });

        foundedUser.ifPresent(loginResponse::setAuthUser);

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody RegisterUserRequestDTO request, HttpServletRequest httpServletRequest) {
        var responseHandler = new GlobalResponseHandler();

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return responseHandler.badRequest("Email already in use", httpServletRequest);
        }

        if (request.getPassword() == null || request.getPassword().length() > 128 || !request.getPassword().matches(GeneralConstants.SECURE_PASSWORD_REGEX)) {
            return responseHandler.badRequest(GeneralConstants.SECURE_PASSWORD_MESSAGE, httpServletRequest);
        }

        Optional<Role> communityRole = roleRepository.findByName(RoleEnum.COMMUNITY_USER);
        Optional<Role> volunteerRole = roleRepository.findByName(RoleEnum.VOLUNTEER_USER);

        if (communityRole.isEmpty()) {
            return responseHandler.badRequest("Community role not found", httpServletRequest);
        }

        if (request.isWantsToBeVolunteer()) {
            if (volunteerRole.isEmpty()) {
                return responseHandler.badRequest("Volunteer role not found", httpServletRequest);
            }
            if (request.getMunicipalityId() == null) {
                return responseHandler.badRequest("Municipality is required for volunteers", httpServletRequest);
            }
        }

        User user = new User();
        user.setName(request.getName());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setIdentificationCard(request.getIdentificationCard());
        user.setBirthDate(request.getBirthDate());
        user.setNeighborhood(Neighborhood.builder().id(request.getNeighborhoodId()).build());

        if (request.getMunicipalityId() != null) {
            user.setMunicipality(Municipality.builder().id(request.getMunicipalityId()).build());
        }

        user.addRole(communityRole.get());
        if (request.isWantsToBeVolunteer()) {
            volunteerRole.ifPresent(user::addRole);
        }

        Set<Long> interestIds = request.getInterestIds();
        if (interestIds != null && !interestIds.isEmpty()) {
            Set<Interest> interests = new HashSet<>(interestRepository.findAllById(interestIds));
            if (interests.size() != interestIds.size()) {
                return responseHandler.badRequest("One or more interests not found", httpServletRequest);
            }
            user.setInterests(interests);
        }

        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }
}