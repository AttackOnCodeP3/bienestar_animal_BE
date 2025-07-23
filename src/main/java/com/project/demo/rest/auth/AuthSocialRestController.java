package com.project.demo.rest.auth;

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
import com.project.demo.rest.auth.dto.CompleteProfileRequestDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/auth/social")
@RequiredArgsConstructor
public class AuthSocialRestController {

    private final RoleRepository roleRepository;
    private final InterestRepository interestRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    private static final Logger logger = LoggerFactory.getLogger(AuthSocialRestController.class);

    /**
     * Handles the success of a social login.
     *
     * @param authentication the authentication object containing user details
     * @param request        HTTP request for metadata
     * @return ResponseEntity with LoginResponse containing the JWT token and user details
     */
    @GetMapping("/success")
    public ResponseEntity<?> onSocialLoginSuccess(
            Authentication authentication,
            HttpServletRequest request) {

        var responseHandler = new GlobalResponseHandler();

        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getAttribute("email");

            logger.info("Login social exitoso para: {}", email);

            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (optionalUser.isEmpty()) {
                return responseHandler.notFound("No se encontró el usuario después del login social.", request);
            }

            User user = optionalUser.get();
            user.setLastLoginDate(LocalDateTime.now());
            userRepository.save(user);

            String token = jwtService.generateToken(user);

            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setAuthUser(user);
            response.setExpiresIn(jwtService.getExpirationTime());

            return responseHandler.success("Inicio de sesión social exitoso.", response, request);

        } catch (Exception e) {
            logger.error("Error durante el login social", e);
            return responseHandler.internalError("Ocurrió un error al procesar el login social.", request);
        }
    }

    /**
     * Completes the user profile after social login.
     *
     * @param request the request containing user details to complete the profile
     * @param httpRequest HTTP request for metadata
     * @return ResponseEntity with LoginResponse containing the JWT token and user details
     */
    @PutMapping("/complete-profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> completeProfile(
            @RequestBody CompleteProfileRequestDTO request,
            HttpServletRequest httpRequest) {

        var responseHandler = new GlobalResponseHandler();

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) authentication.getPrincipal();

            logger.info("Completando perfil para usuario con ID: {}", currentUser.getId());

            User user = userRepository.findById(currentUser.getId()).orElse(null);
            if (user == null) {
                return responseHandler.notFound("Usuario autenticado no encontrado.", httpRequest);
            }

            Optional<Role> communityRole = roleRepository.findByName(RoleEnum.COMMUNITY_USER);
            Optional<Role> volunteerRole = roleRepository.findByName(RoleEnum.VOLUNTEER_USER);

            if (communityRole.isEmpty()) {
                return responseHandler.badRequest("No se encontró el rol de usuario comunitario.", httpRequest);
            }

            if (request.isWantsToBeVolunteer()) {
                if (volunteerRole.isEmpty()) {
                    return responseHandler.badRequest("No se encontró el rol de voluntario.", httpRequest);
                }
                if (request.getMunicipalityId() == null) {
                    return responseHandler.badRequest("La municipalidad es obligatoria para ser voluntario.", httpRequest);
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

            if (user.getRoles().stream().noneMatch(r -> r.getName().equals(RoleEnum.COMMUNITY_USER))) {
                user.addRole(communityRole.get());
            }

            if (request.isWantsToBeVolunteer()) {
                if (user.getRoles().stream().noneMatch(r -> r.getName().equals(RoleEnum.VOLUNTEER_USER))) {
                    if (volunteerRole.isEmpty()) {
                        return responseHandler.badRequest("No se encontró el rol de voluntario.", httpRequest);
                    }
                    user.addRole(volunteerRole.get());
                }
            }

            Set<Long> interestIds = request.getInterestIds();
            if (interestIds != null && !interestIds.isEmpty()) {
                Set<Interest> interests = new HashSet<>(interestRepository.findAllById(interestIds));
                if (interests.size() != interestIds.size()) {
                    return responseHandler.badRequest("Uno o más intereses especificados no existen.", httpRequest);
                }
                user.setInterests(interests);
            }

            user.setSocialLoginCompleted(true);
            user.setLastLoginDate(LocalDateTime.now());

            User savedUser = userRepository.save(user);
            String token = jwtService.generateToken(savedUser);

            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setToken(token);
            loginResponse.setAuthUser(savedUser);
            loginResponse.setExpiresIn(jwtService.getExpirationTime());

            return responseHandler.success("Perfil social completado exitosamente.", loginResponse, httpRequest);

        } catch (Exception e) {
            logger.error("Error al completar el perfil del usuario social", e);
            return responseHandler.internalError("Ocurrió un error al completar el perfil del usuario.", httpRequest);
        }
    }
}
