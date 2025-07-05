package com.project.demo.rest.user;

import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.interest.Interest;
import com.project.demo.logic.entity.interest.InterestRepository;
import com.project.demo.logic.entity.municipality.Municipality;
import com.project.demo.logic.entity.neighborhood.Neighborhood;
import com.project.demo.logic.entity.rol.Role;
import com.project.demo.logic.entity.rol.RoleEnum;
import com.project.demo.logic.entity.rol.RoleRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.rest.auth.dto.CompleteProfileRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/users")
public class UserRestController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page-1, size);
        Page<User> usersPage = userRepository.findAll(pageable);
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(usersPage.getTotalPages());
        meta.setTotalElements(usersPage.getTotalElements());
        meta.setPageNumber(usersPage.getNumber() + 1);
        meta.setPageSize(usersPage.getSize());

        return new GlobalResponseHandler().handleResponse("Users retrieved successfully",
                usersPage.getContent(), HttpStatus.OK, meta);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> addUser(@RequestBody User user, HttpServletRequest request) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return new GlobalResponseHandler().handleResponse("User updated successfully",
                user, HttpStatus.OK, request);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody User user, HttpServletRequest request) {
        Optional<User> foundOrder = userRepository.findById(userId);
        if(foundOrder.isPresent()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            return new GlobalResponseHandler().handleResponse("User updated successfully",
                    user, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("User id " + userId + " not found"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PutMapping("/complete-profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> completeProfile(@RequestBody CompleteProfileRequestDTO request, HttpServletRequest httpRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Optional<User> userOptional = userRepository.findById(currentUser.getId());
        if (userOptional.isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "Authenticated user not found", HttpStatus.NOT_FOUND, httpRequest);
        }

        User user = userOptional.get();

        Optional<Role> communityRole = roleRepository.findByName(RoleEnum.COMMUNITY_USER);
        Optional<Role> volunteerRole = roleRepository.findByName(RoleEnum.VOLUNTEER_USER);

        if (communityRole.isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "Community user role not found", HttpStatus.BAD_REQUEST, httpRequest);
        }

        if (request.isWantsToBeVolunteer()) {
            if (volunteerRole.isEmpty()) {
                return new GlobalResponseHandler().handleResponse(
                        "Volunteer role not found", HttpStatus.BAD_REQUEST, httpRequest);
            }
            if (request.getMunicipalityId() == null) {
                return new GlobalResponseHandler().handleResponse(
                        "Municipality is required if user wants to be a volunteer", HttpStatus.BAD_REQUEST, httpRequest);
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
            Role role = communityRole.orElseThrow(() ->
                    new IllegalStateException("Community role should not be missing at this point")
            );
            user.addRole(role);
        }

        if (request.isWantsToBeVolunteer()) {
            if (user.getRoles().stream().noneMatch(role -> role.getName().equals(RoleEnum.VOLUNTEER_USER))) {
                Role role = volunteerRole.orElseThrow(() ->
                        new IllegalStateException("Volunteer role should not be missing at this point")
                );
                user.addRole(role);
            }
        }

        Set<Long> interestIds = request.getInterestIds();
        if (interestIds != null && !interestIds.isEmpty()) {
            Set<Interest> interests = new HashSet<>(interestRepository.findAllById(interestIds));
            if (interests.size() != interestIds.size()) {
                return new GlobalResponseHandler().handleResponse(
                        "One or more interests were not found", HttpStatus.BAD_REQUEST, httpRequest);
            }
            user.setInterests(interests);
        }

        user.setSocialLoginCompleted(true);

        User savedUser = userRepository.save(user);

        return new GlobalResponseHandler().handleResponse(
                "Profile completed successfully", savedUser, HttpStatus.OK, httpRequest);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId, HttpServletRequest request) {
        Optional<User> foundOrder = userRepository.findById(userId);
        if(foundOrder.isPresent()) {
            userRepository.deleteById(userId);
            return new GlobalResponseHandler().handleResponse("User deleted successfully",
                    foundOrder.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("User id " + userId + " not found"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public User authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

}