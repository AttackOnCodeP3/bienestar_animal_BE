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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthRestController {

    private static final Logger logger = LoggerFactory.getLogger(AuthRestController.class);

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private RoleRepository roleRepository;
    @Autowired private InterestRepository interestRepository;

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;

    public AuthRestController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody User user, HttpServletRequest request) {
        var responseHandler = new GlobalResponseHandler();
        try {
            logger.info("Autenticando usuario con email: {}", user.getEmail());

            User authenticatedUser = authenticationService.authenticate(user);
            String jwtToken = jwtService.generateToken(authenticatedUser);

            authenticatedUser.setLastLoginDate(LocalDateTime.now());
            userRepository.save(authenticatedUser);

            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setToken(jwtToken);
            loginResponse.setExpiresIn(jwtService.getExpirationTime());
            loginResponse.setAuthUser(authenticatedUser);

            return responseHandler.success("Inicio de sesión exitoso.", loginResponse, request);

        } catch (Exception e) {
            logger.error("Error al autenticar usuario", e);
            return responseHandler.internalError("Ocurrió un error al autenticar el usuario.", request);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(
            @RequestBody RegisterUserRequestDTO request,
            HttpServletRequest httpRequest) {

        var responseHandler = new GlobalResponseHandler();
        try {
            logger.info("Intentando registrar nuevo usuario con email: {}", request.getEmail());

            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return responseHandler.badRequest("El correo electrónico ya está en uso.", httpRequest);
            }

            if (request.getPassword() == null ||
                    request.getPassword().length() > 128 ||
                    !request.getPassword().matches(GeneralConstants.SECURE_PASSWORD_REGEX)) {
                return responseHandler.badRequest(GeneralConstants.SECURE_PASSWORD_MESSAGE, httpRequest);
            }

            Optional<Role> communityRoleOpt = roleRepository.findByName(RoleEnum.COMMUNITY_USER);
            Optional<Role> volunteerRoleOpt = roleRepository.findByName(RoleEnum.VOLUNTEER_USER);

            if (communityRoleOpt.isEmpty()) {
                return responseHandler.badRequest("No se encontró el rol de usuario comunitario.", httpRequest);
            }

            if (request.isWantsToBeVolunteer()) {
                if (volunteerRoleOpt.isEmpty()) {
                    return responseHandler.badRequest("No se encontró el rol de voluntario.", httpRequest);
                }
                if (request.getMunicipalityId() == null) {
                    return responseHandler.badRequest("La municipalidad es obligatoria para ser voluntario.", httpRequest);
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

            Role communityRole = communityRoleOpt.get();
            user.addRole(communityRole);

            if (request.isWantsToBeVolunteer()) {
                volunteerRoleOpt.ifPresent(user::addRole);
            }

            Set<Long> interestIds = request.getInterestIds();
            if (interestIds != null && !interestIds.isEmpty()) {
                Set<Interest> interests = new HashSet<>(interestRepository.findAllById(interestIds));
                if (interests.size() != interestIds.size()) {
                    return responseHandler.badRequest("Uno o más intereses especificados no existen.", httpRequest);
                }
                user.setInterests(interests);
            }

            User savedUser = userRepository.save(user);

            logger.info("Usuario registrado exitosamente con ID: {}", savedUser.getId());
            return responseHandler.created("Usuario registrado exitosamente.", savedUser, httpRequest);

        } catch (Exception e) {
            logger.error("Error al registrar usuario", e);
            return responseHandler.internalError("Ocurrió un error al registrar el usuario.", httpRequest);
        }
    }
}