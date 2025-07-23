package com.project.demo.rest.user;

import com.project.demo.common.BooleanUtils;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
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
import com.project.demo.rest.user.dto.UpdateUserRequestDTO;
import jakarta.persistence.EntityNotFoundException;
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private NeighborhoodRepository neighborhoodRepository;

    @Autowired
    private MunicipalityRepository municipalityRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        int safePage = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(safePage, size);

        Role superAdminRole = roleRepository.findByName(RoleEnum.SUPER_ADMIN)
                .orElseThrow(() -> new RuntimeException("Rol SUPER_ADMIN no encontrado"));

        Page<User> usersPage = userRepository.findAllExcludingUsersWithRoleId(
                superAdminRole.getId(), pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(usersPage.getTotalPages());
        meta.setTotalElements(usersPage.getTotalElements());
        meta.setPageNumber(usersPage.getNumber() + 1);
        meta.setPageSize(usersPage.getSize());

        return new GlobalResponseHandler().handleResponse("Users retrieved successfully",
                usersPage.getContent(), HttpStatus.OK, meta);
    }

    /**
     * Fetches a user by their ID.
     *
     * @param userId the ID of the user to fetch
     * @param request the HTTP request for metadata
     * @return a ResponseEntity containing the user or an error message
     * @author dgutierrez
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> getUserById(
            @PathVariable Long userId,
            HttpServletRequest request) {

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "User id " + userId + " not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }

        return new GlobalResponseHandler().handleResponse(
                "User retrieved successfully",
                userOpt.get(),
                HttpStatus.OK,
                request
        );
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @RequestBody UpdateUserRequestDTO dto,
            HttpServletRequest request) {

        Optional<User> foundUserOpt = userRepository.findById(userId);
        if (foundUserOpt.isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "User id " + userId + " not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }

        if (dto.getRoleIds() != null && dto.getRoleIds().isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "At least one role must be selected",
                    HttpStatus.BAD_REQUEST,
                    request
            );
        }

        User currentUser = foundUserOpt.get();

        Municipality municipality = null;
        if (dto.getMunicipalityId() != null) {
            municipality = municipalityRepository.findById(dto.getMunicipalityId())
                    .orElseThrow(() -> new EntityNotFoundException("Municipality not found"));
        }

        Neighborhood neighborhood = null;
        if (dto.getNeighborhoodId() != null) {
            neighborhood = neighborhoodRepository.findById(dto.getNeighborhoodId())
                    .orElseThrow(() -> new EntityNotFoundException("Neighborhood not found"));
        }

        Set<Interest> interests = dto.getInterestIds() != null
                ? new HashSet<>(interestRepository.findAllById(dto.getInterestIds()))
                : currentUser.getInterests();

        Set<Role> roles = dto.getRoleIds() != null
                ? new HashSet<>(roleRepository.findAllById(dto.getRoleIds()))
                : currentUser.getRoles();

        User updatedUser = User.builder()
                .id(currentUser.getId())
                .identificationCard(dto.getIdentificationCard())
                .name(dto.getName())
                .lastname(dto.getLastname())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .birthDate(dto.getBirthDate())
                .nurseryHome(Boolean.TRUE.equals(dto.getNurseryHome()))
                .requiresPasswordChange(Boolean.TRUE.equals(dto.getRequiresPasswordChange()))
                .active(Boolean.TRUE.equals(dto.getActive()))
                .registeredByCensusTaker(Boolean.TRUE.equals(dto.getRegisteredByCensusTaker()))
                .socialLoginCompleted(Boolean.TRUE.equals(dto.getSocialLoginCompleted()))
                .usedSocialLogin(Boolean.TRUE.equals(dto.getUsedSocialLogin()))
                .municipality(municipality)
                .neighborhood(neighborhood)
                .interests(interests)
                .roles(roles)

                .password(currentUser.getPassword())
                .temporaryPassword(currentUser.getTemporaryPassword())
                .lastLoginDate(currentUser.getLastLoginDate())
                .createdAt(currentUser.getCreatedAt())
                .build();

        userRepository.save(updatedUser);

        return new GlobalResponseHandler().handleResponse(
                "User updated successfully",
                updatedUser,
                HttpStatus.OK,
                request
        );
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