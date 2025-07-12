package com.project.demo.rest.municipality;

import com.project.demo.logic.entity.canton.Canton;
import com.project.demo.logic.entity.canton.CantonRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.municipality.*;
import com.project.demo.logic.entity.rol.Role;
import com.project.demo.logic.entity.rol.RoleEnum;
import com.project.demo.logic.entity.rol.RoleRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.rest.municipality.dto.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import org.slf4j.Logger;
@RestController
@RequestMapping("/municipalities")
public class MunicipalityRestController {

    @Autowired
    private MunicipalityRepository municipalityRepository;

    @Autowired
    private CantonRepository cantonRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MunicipalityStatusRepository municipalityStatusRepository;

    private final Logger logger = LoggerFactory.getLogger(MunicipalityRestController.class);

    /**
     *
     * @param name
     * @param cantonId
     * @param status
     * @param page
     * @param size
     * @param request
     * @return
     * @author gjimienez
     * {@code @modifiedBy} dgutierrez (12/7/2025) - Add JavaDoc comments and the logger
     */
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long cantonId,
            @RequestParam(required = false) MunicipalityStatusEnum status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        logger.info("Fetching municipalities with filters: name={}, cantonId={}, status={}", name, cantonId, status);
        Pageable pageable = PageRequest.of(page - 1, size);
        Specification<Municipality> spec = Specification
                .where(MunicipalitySpecifications.hasNameContaining(name))
                .and(MunicipalitySpecifications.hasCantonId(cantonId))
                .and(MunicipalitySpecifications.hasStatus(status));

        Page<Municipality> municipalityPage = municipalityRepository.findAll(spec, pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(municipalityPage.getTotalPages());
        meta.setTotalElements(municipalityPage.getTotalElements());
        meta.setPageNumber(municipalityPage.getNumber() + 1);
        meta.setPageSize(municipalityPage.getSize());

        return new GlobalResponseHandler().handleResponse(
                "Municipalities retrieved successfully",
                municipalityPage.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    /**
     *
     * @param municipalityId
     * @param request
     * @return
     * @author gjimienez
     * {@code @modifiedBy} dgutierrez (12/7/2025) - Add JavaDoc comments and the logger
     */
    @GetMapping("/{municipalityId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getById(@PathVariable Long municipalityId, HttpServletRequest request) {
        logger.info("Fetching municipality with ID: {}", municipalityId);
        Optional<Municipality> optional = municipalityRepository.findById(municipalityId);

        if (optional.isPresent()) {
            return new GlobalResponseHandler().handleResponse(
                    "Municipality retrieved successfully",
                    optional.get(),
                    HttpStatus.OK,
                    request
            );
        } else {
            return new GlobalResponseHandler().handleResponse(
                    "Municipality not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }
    }

    /**
     * Creates a new municipality and its associated admin user.
     * <p>
     * Validates that the canton is not already associated with another municipality,
     * and that the user email is not already registered. If either check fails, a
     * BAD_REQUEST response is returned.
     * <p>
     * On success, the method creates the municipality, an admin user associated with it,
     * and returns a CREATED response with the saved municipality.
     *
     * @param createMunicipalityRequestDTO the request data used to create the municipality
     * @param request the incoming HTTP servlet request (used to build metadata)
     * @return a ResponseEntity with success or error information
     *
     * @author gjimienez
     * {@code @modifiedBy} dgutierrez (12/7/2025) - Added validations for canton and user email uniqueness,
     *                                       applied builder pattern and structured logging.
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> create(
            @RequestBody CreateMunicipalityRequestDTO createMunicipalityRequestDTO,
            HttpServletRequest request
    ) {
        logger.info("Creating municipality with name: {}", createMunicipalityRequestDTO.getName());
        var globalResponseHandler = new GlobalResponseHandler();

        if (municipalityRepository.findByEmail(createMunicipalityRequestDTO.getEmail()).isPresent()) {
            return globalResponseHandler.handleResponse(
                    "A municipality with this email already exists",
                    HttpStatus.BAD_REQUEST,
                    request
            );
        }

        if (municipalityRepository.existsByCantonId(createMunicipalityRequestDTO.getCantonId())) {
            return globalResponseHandler.handleResponse(
                    "A municipality for this canton already exists",
                    HttpStatus.BAD_REQUEST,
                    request
            );
        }

        if (userRepository.findByEmail(createMunicipalityRequestDTO.getEmail()).isPresent()) {
            return globalResponseHandler.handleResponse(
                    "A user with this email already exists",
                    HttpStatus.BAD_REQUEST,
                    request
            );
        }

        Role adminRole = roleRepository.findByName(RoleEnum.MUNICIPAL_ADMIN)
                .orElseThrow(() -> new RuntimeException(RoleEnum.MUNICIPAL_ADMIN.name() + " role not found"));

        MunicipalityStatus activeStatus = municipalityStatusRepository
                .findByName(MunicipalityStatusEnum.ACTIVE.getDisplayName())
                .orElseThrow(() -> new IllegalArgumentException("Municipality status ACTIVE not found"));

        Canton canton = cantonRepository.findById(createMunicipalityRequestDTO.getCantonId())
                .orElseThrow(() -> new IllegalArgumentException("Canton not found"));

        Municipality municipality = Municipality.builder()
                .name(createMunicipalityRequestDTO.getName())
                .address(createMunicipalityRequestDTO.getAddress())
                .phone(createMunicipalityRequestDTO.getPhone())
                .email(createMunicipalityRequestDTO.getEmail())
                .responsibleName(createMunicipalityRequestDTO.getResponsibleName())
                .responsibleRole(createMunicipalityRequestDTO.getResponsibleRole())
                .status(activeStatus)
                .canton(canton)
                .build();

        Municipality savedMunicipality = municipalityRepository.save(municipality);

        User adminUser = User.builder()
                .name(createMunicipalityRequestDTO.getResponsibleName())
                .lastname(createMunicipalityRequestDTO.getResponsibleRole())
                .email(createMunicipalityRequestDTO.getEmail())
                .password(passwordEncoder.encode("123")) // default password
                .requiresPasswordChange(true)
                .municipality(savedMunicipality)
                .build();

        adminUser.addRole(adminRole);
        userRepository.save(adminUser);

        return globalResponseHandler.handleResponse(
                "Municipality and admin user created successfully",
                savedMunicipality,
                HttpStatus.CREATED,
                request
        );
    }

    /**
     *
     * @param municipalityId
     * @param dto
     * @param request
     * @return
     * @author gjimienez
     * {@code @modifiedBy} dgutierrez (12/7/2025) - Add JavaDoc comments and the logger
     */
    @PutMapping("/{municipalityId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> update(
            @PathVariable Long municipalityId,
            @RequestBody UpdateMunicipalityRequestDTO dto,
            HttpServletRequest request
    ) {
        logger.info("Updating municipality with ID: {}", municipalityId);

        Municipality municipality = municipalityRepository.findById(municipalityId)
                .orElseThrow(() -> new EntityNotFoundException("Municipality not found"));

        MunicipalityStatus status = municipalityStatusRepository.findById(dto.getMunicipalityStatusId())
                .orElseThrow(() -> new EntityNotFoundException("Municipality status not found"));

        Canton canton = cantonRepository.findById(dto.getCantonId())
                .orElseThrow(() -> new EntityNotFoundException("Canton not found"));

        municipality.updateFromDto(dto, status, canton);

        Municipality updated = municipalityRepository.save(municipality);

        return new GlobalResponseHandler().handleResponse(
                "Municipality updated successfully",
                updated,
                HttpStatus.OK,
                request
        );
    }

    /**
     *
     * @param municipalityId
     * @param updateMunicipalityStatusRequestDTO
     * @param request
     * @return
     * @author gjimienez
     * {@code @modifiedBy} dgutierrez (12/7/2025) - Add JavaDoc comments and the logger
     */
    @PatchMapping("/{municipalityId}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long municipalityId,
            @RequestBody UpdateMunicipalityStatusRequestDTO updateMunicipalityStatusRequestDTO,
            HttpServletRequest request
    ) {
        logger.info("Updating status for municipality with ID: {}", municipalityId);
        Optional<Municipality> optional = municipalityRepository.findById(municipalityId);
        if (optional.isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "Municipality not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }

        Municipality municipality = optional.get();
        municipality.setStatus(
                municipalityStatusRepository.findById(updateMunicipalityStatusRequestDTO.getMunicipalityStatusId())
                        .orElseThrow(() -> new IllegalArgumentException("Municipality status not found"))
        );
        municipalityRepository.save(municipality);

        return new GlobalResponseHandler().handleResponse(
                "Municipality status updated successfully",
                municipality,
                HttpStatus.OK,
                request
        );
    }

    /**
     *
     * @param municipalityId
     * @param requestDTO
     * @param request
     * @return
     * @author gjimienez
     * {@code @modifiedBy} dgutierrez (12/7/2025) - Add JavaDoc comments and the logger
     */
    @PatchMapping("/{municipalityId}/responsible")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateResponsible(
            @PathVariable Long municipalityId,
            @RequestBody UpdateResponsibleRequestDTO requestDTO,
            HttpServletRequest request
    ) {
        logger.info("Updating responsible for municipality with ID: {}", municipalityId);
        Optional<Municipality> optional = municipalityRepository.findById(municipalityId);
        if (optional.isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "Municipality not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }

        Municipality municipality = optional.get();
        municipality.setResponsibleName(requestDTO.getResponsibleName());
        municipality.setResponsibleRole(requestDTO.getResponsibleRole());
        municipalityRepository.save(municipality);

        return new GlobalResponseHandler().handleResponse(
                "Responsible updated successfully",
                municipality,
                HttpStatus.OK,
                request
        );
    }

    /**
     *
     * @param municipalityId
     * @param requestDTO
     * @param request
     * @return
     * @author gjimienez
     * {@code @modifiedBy} dgutierrez (12/7/2025) - Add JavaDoc comments and the logger
     */
    @PatchMapping("/{municipalityId}/logo")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateLogo(
            @PathVariable Long municipalityId,
            @RequestBody UpdateLogoRequestDTO requestDTO,
            HttpServletRequest request
    ) {
        logger.info("Updating logo for municipality with ID: {}", municipalityId);
        Optional<Municipality> optional = municipalityRepository.findById(municipalityId);
        if (optional.isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "Municipality not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }

        Municipality municipality = optional.get();
        municipality.setLogo(requestDTO.getLogo());
        municipalityRepository.save(municipality);

        return new GlobalResponseHandler().handleResponse(
                "Logo updated successfully",
                municipality,
                HttpStatus.OK,
                request
        );
    }

    /**
     *
     * @param municipalityId
     * @param request
     * @return
     * @author gjimienez
     * {@code @modifiedBy} dgutierrez (12/7/2025) - Add JavaDoc comments and the logger
     */
    @DeleteMapping("/{municipalityId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long municipalityId, HttpServletRequest request) {
        logger.info("Deleting municipality with ID: {}", municipalityId);
        Optional<Municipality> found = municipalityRepository.findById(municipalityId);
        if (found.isPresent()) {
            municipalityRepository.deleteById(municipalityId);
            return new GlobalResponseHandler().handleResponse(
                    "Municipality deleted successfully",
                    found.get(),
                    HttpStatus.OK,
                    request
            );
        } else {
            return new GlobalResponseHandler().handleResponse(
                    "Municipality not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }
    }
}
