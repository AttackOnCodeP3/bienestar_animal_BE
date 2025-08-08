package com.project.demo.rest.user;

import com.project.demo.common.PaginationUtils;
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
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * @modifiedBy gjimenez - Added new GET endpoint to support community animal owner lookup.
 */

@RestController
@RequestMapping("/users")
public class UserRestController {

    private static final Logger logger = LoggerFactory.getLogger(UserRestController.class);

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

        logger.info("Invocación de GET /users - Listar usuarios (página {}, tamaño {})", page, size);
        var globalResponseHandler = new GlobalResponseHandler();

        int safePage = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(safePage, size);

        Optional<Role> superAdminRoleOpt = roleRepository.findByName(RoleEnum.SUPER_ADMIN);
        if (superAdminRoleOpt.isEmpty()) {
            return globalResponseHandler.notFound(
                    "El rol SUPER_ADMIN no fue encontrado",
                    request
            );
        }

        Page<User> usersPage = userRepository.findAllExcludingUsersWithRoleId(
                superAdminRoleOpt.get().getId(), pageable);

        Meta meta = PaginationUtils.buildMeta(request, usersPage);

        return globalResponseHandler.handleResponse(
                "Usuarios obtenidos correctamente",
                usersPage.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> getUserById(
            @PathVariable Long userId,
            HttpServletRequest request) {

        logger.info("Invocación de GET /users/{} - Obtener usuario por ID", userId);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return globalResponseHandler.handleResponse(
                    "El usuario con ID " + userId + " no fue encontrado",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }

        return globalResponseHandler.handleResponse(
                "Usuario obtenido correctamente",
                userOpt.get(),
                HttpStatus.OK,
                request
        );
    }

    @GetMapping("/cedula/{cedula}")
    @PreAuthorize("hasAnyRole('CENSISTA_USER', 'SUPER_ADMIN')")
    public ResponseEntity<?> getUserByCedula(
            @PathVariable String cedula,
            HttpServletRequest request) {

        var globalResponseHandler = new GlobalResponseHandler();

        Optional<User> userOpt = userRepository.findByIdentificationCard(cedula);
        if (userOpt.isEmpty()) {
            return globalResponseHandler.handleResponse(
                    "No se encontro un usuario con la cedula proporcionada",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }
        return globalResponseHandler.handleResponse(
                "Usuario encontrado",
                userOpt.get(),
                HttpStatus.OK,
                request
        );
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> addUser(@RequestBody User user, HttpServletRequest request) {

        logger.info("Invocación de POST /users - Crear usuario con correo: {}", user.getEmail());
        var globalResponseHandler = new GlobalResponseHandler();

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        return globalResponseHandler.handleResponse(
                "Usuario creado correctamente",
                user,
                HttpStatus.OK,
                request
        );
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @RequestBody UpdateUserRequestDTO dto,
            HttpServletRequest request) {

        logger.info("Invocación de PUT /users/{} - Actualizar usuario", userId);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return globalResponseHandler.notFound(
                    "El usuario con ID " + userId + " no fue encontrado",
                    request
            );
        }

        if (dto.getRoleIds() != null && dto.getRoleIds().isEmpty()) {
            return globalResponseHandler.badRequest(
                    "Debe asignarse al menos un rol al usuario",
                    request
            );
        }

        Municipality municipality = null;
        if (dto.getMunicipalityId() != null) {
            municipality = municipalityRepository.findById(dto.getMunicipalityId())
                    .orElse(null);

            if (municipality == null) {
                return globalResponseHandler.notFound(
                        "La municipalidad con ID " + dto.getMunicipalityId() + " no fue encontrado",
                        request
                );
            }
        }

        Neighborhood neighborhood = null;
        if (dto.getNeighborhoodId() != null) {
            neighborhood = neighborhoodRepository.findById(dto.getNeighborhoodId())
                    .orElse(null);

            if (neighborhood == null) {
                return globalResponseHandler.notFound(
                        "El barrio con ID " + dto.getNeighborhoodId() + " no fue encontrado",
                        request
                );
            }
        }

        User currentUser = userOpt.get();

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

        return globalResponseHandler.handleResponse(
                "Usuario actualizado correctamente",
                updatedUser,
                HttpStatus.OK,
                request
        );
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId, HttpServletRequest request) {

        logger.info("Invocación de DELETE /users/{} - Eliminar usuario", userId);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<User> foundUser = userRepository.findById(userId);
        if (foundUser.isPresent()) {
            userRepository.deleteById(userId);
            return globalResponseHandler.handleResponse(
                    "Usuario eliminado correctamente",
                    foundUser.get(),
                    HttpStatus.OK,
                    request
            );
        }

        return globalResponseHandler.handleResponse(
                "Usuario con ID " + userId + " no encontrado",
                HttpStatus.NOT_FOUND,
                request
        );
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public User authenticatedUser() {
        logger.info("Invocación de GET /users/me - Obtener usuario autenticado");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}