package com.project.demo.rest.user;

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
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
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

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> addUser(@RequestBody User user, HttpServletRequest request) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return new GlobalResponseHandler().handleResponse("User updated successfully",
                user, HttpStatus.OK, request);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @RequestBody UpdateUserRequestDTO updateUserRequestDTO,
            HttpServletRequest request) {

        Optional<User> foundUserOpt = userRepository.findById(userId);
        if (foundUserOpt.isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "User id " + userId + " not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }

        User user = foundUserOpt.get();

        // Actualización de campos básicos
        user.setIdentificationCard(updateUserRequestDTO.getIdentificationCard());
        user.setName(updateUserRequestDTO.getName());
        user.setLastname(updateUserRequestDTO.getLastname());
        user.setEmail(updateUserRequestDTO.getEmail());
        user.setPhoneNumber(updateUserRequestDTO.getPhoneNumber());
        user.setBirthDate(updateUserRequestDTO.getBirthDate());
        user.setNurseryHome(Boolean.TRUE.equals(updateUserRequestDTO.getNurseryHome()));
        user.setRequiresPasswordChange(Boolean.TRUE.equals(updateUserRequestDTO.getRequiresPasswordChange()));
        user.setActive(Boolean.TRUE.equals(updateUserRequestDTO.getActive()));
        user.setRegisteredByCensusTaker(Boolean.TRUE.equals(updateUserRequestDTO.getRegisteredByCensusTaker()));
        user.setSocialLoginCompleted(Boolean.TRUE.equals(updateUserRequestDTO.getSocialLoginCompleted()));
        user.setUsedSocialLogin(Boolean.TRUE.equals(updateUserRequestDTO.getUsedSocialLogin()));

        if (updateUserRequestDTO.getMunicipalityId() != null) {
            Municipality municipality = municipalityRepository.findById(updateUserRequestDTO.getMunicipalityId())
                    .orElseThrow(() -> new EntityNotFoundException("Municipality not found"));
            user.setMunicipality(municipality);
        }

        if (updateUserRequestDTO.getNeighborhoodId() != null) {
            Neighborhood neighborhood = neighborhoodRepository.findById(updateUserRequestDTO.getNeighborhoodId())
                    .orElseThrow(() -> new EntityNotFoundException("Neighborhood not found"));
            user.setNeighborhood(neighborhood);
        }

        if (updateUserRequestDTO.getInterestIds() != null) {
            Set<Interest> interests = new HashSet<>(interestRepository.findAllById(updateUserRequestDTO.getInterestIds()));
            user.setInterests(interests);
        }

        if (updateUserRequestDTO.getRoleIds() != null) {
            Set<Role> roles = new HashSet<>();
            roleRepository.findAllById(updateUserRequestDTO.getRoleIds()).forEach(roles::add);
        }

        userRepository.save(user);

        return new GlobalResponseHandler().handleResponse(
                "User updated successfully",
                user,
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