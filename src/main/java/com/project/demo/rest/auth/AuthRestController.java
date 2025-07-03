package com.project.demo.rest.auth;

import com.project.demo.logic.entity.auth.AuthenticationService;
import com.project.demo.logic.entity.auth.JwtService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

        foundedUser.ifPresent(loginResponse::setAuthUser);

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody RegisterUserRequestDTO request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already in use");
        }

        Optional<Role> communityRole = roleRepository.findByName(RoleEnum.COMMUNITY_USER);
        Optional<Role> volunteerRole = roleRepository.findByName(RoleEnum.VOLUNTEER_USER);

        if (communityRole.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Community role not found");
        }

        if (request.isWantsToBeVolunteer() && volunteerRole.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Volunteer role not found");
        }

        User user = new User();
        user.setName(request.getName());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setIdentificationCard(request.getIdentificationCard());
        user.setNeighborhood(Neighborhood.builder().id(request.getNeighborhoodId()).build());
        user.setBirthDate(request.getBirthDate());

        if (request.getMunicipalityId() != null) {
            user.setMunicipality(Municipality.builder().id(request.getMunicipalityId()).build());
        }

        user.addRole(communityRole.get());
        if (request.isWantsToBeVolunteer() && volunteerRole.isPresent()) {
            user.addRole(volunteerRole.get());
        }

        Set<Interest> interests = new HashSet<>();
        if (request.getInterestIds() != null && !request.getInterestIds().isEmpty()) {
            interests = new HashSet<>(interestRepository.findAllById(request.getInterestIds()));
            if (interests.size() != request.getInterestIds().size()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("One or more interests not found");
            }
        }
        user.setInterests(interests);

        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

}