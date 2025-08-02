package com.project.demo.rest.municipality;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.canton.Canton;
import com.project.demo.logic.entity.canton.CantonRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.municipality.*;
import com.project.demo.logic.entity.rol.Role;
import com.project.demo.logic.entity.rol.RoleEnum;
import com.project.demo.logic.entity.rol.RoleRepository;
import com.project.demo.logic.entity.user.ForgotPasswordService;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.rest.municipality.dto.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;

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
    @Autowired
    private ForgotPasswordService forgotPasswordService;

    private final Logger logger = LoggerFactory.getLogger(MunicipalityRestController.class);

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

        Meta meta = PaginationUtils.buildMeta(request, municipalityPage);

        return new GlobalResponseHandler().handleResponse(
                "Municipalities retrieved successfully",
                municipalityPage.getContent(),
                HttpStatus.OK,
                meta
        );
    }

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

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> create(
            @RequestBody CreateMunicipalityRequestDTO dto,
            HttpServletRequest request
    ) {
        logger.info("Creating municipality with name: {}", dto.getName());
        var globalResponseHandler = new GlobalResponseHandler();
        try {
            validarMunicipality(dto.getName(), dto.getEmail(), dto.getCantonId(), null);
        } catch (IllegalArgumentException ex) {
            return new GlobalResponseHandler().handleResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
        }

        Optional<Role> adminRole = roleRepository.findByName(RoleEnum.MUNICIPAL_ADMIN);

        if (adminRole.isEmpty()) {
            return globalResponseHandler.badRequest(
                    "Role " + RoleEnum.MUNICIPAL_ADMIN.name() + " not found",
                    request
            );
        }


        Optional<MunicipalityStatus> activeStatus = municipalityStatusRepository
                .findByName(MunicipalityStatusEnum.ACTIVE.getDisplayName());

        if (activeStatus.isEmpty()) {
            return globalResponseHandler.badRequest(
                    "Municipality status ACTIVE not found",
                    request
            );
        }

        Optional<Canton> canton = cantonRepository.findById(dto.getCantonId());

        if (canton.isEmpty()) {
            return globalResponseHandler.badRequest(
                    "Canton with ID " + dto.getCantonId() + " not found",
                    request
            );
        }

        Municipality municipality = Municipality.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .responsibleName(dto.getResponsibleName())
                .responsibleRole(dto.getResponsibleRole())
                .status(activeStatus.get())
                .canton(canton.get())
                .build();

        Municipality saved = municipalityRepository.save(municipality);

        User adminUser = User.builder()
                .name(dto.getResponsibleName())
                .lastname(dto.getResponsibleRole())
                .email(dto.getEmail())
                .password(passwordEncoder.encode("123"))
                .requiresPasswordChange(true)
                .municipality(saved)
                .active(true)
                .roles(new HashSet<>())
                .build();

        adminUser.addRole(adminRole.get());
        userRepository.save(adminUser);

        try {
            forgotPasswordService.resetPasswordAndSendEmail(dto.getEmail());
        } catch (Exception e) {
            logger.error("Error sending email to admin user: {}", e.getMessage());
        }

        return new GlobalResponseHandler().handleResponse(
                "Municipality and admin user created successfully",
                saved,
                HttpStatus.CREATED,
                request
        );
    }

    @PutMapping("/{municipalityId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> update(
            @PathVariable Long municipalityId,
            @RequestBody UpdateMunicipalityRequestDTO dto,
            HttpServletRequest request
    ) {
        logger.info("Updating municipality with ID: {}", municipalityId);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<Municipality> municipality = municipalityRepository.findById(municipalityId);
        if (municipality.isEmpty()) {
            return globalResponseHandler.handleResponse(
                    "Municipality with ID " + municipalityId + " not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }
        try {
            validarMunicipality(dto.getName(), dto.getEmail(), dto.getCantonId(), municipalityId);
        } catch (IllegalArgumentException ex) {
            return globalResponseHandler.handleResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
        }

        Optional<MunicipalityStatus> status = municipalityStatusRepository.findById(dto.getMunicipalityStatusId());
        if (status.isEmpty()) {
            return globalResponseHandler.handleResponse(
                    "Municipality status not found",
                    HttpStatus.BAD_REQUEST,
                    request
            );
        }

        Optional<Canton> canton = cantonRepository.findById(dto.getCantonId());
        if (canton.isEmpty()) {
            return globalResponseHandler.handleResponse(
                    "Canton with ID " + dto.getCantonId() + " not found",
                    HttpStatus.BAD_REQUEST,
                    request
            );
        }

        municipality.get().updateFromDto(dto, status.get(), canton.get());
        Municipality updated = municipalityRepository.save(municipality.get());

        return globalResponseHandler.handleResponse(
                "Municipality updated successfully",
                updated,
                HttpStatus.OK,
                request
        );
    }

    @PatchMapping("/{municipalityId}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long municipalityId,
            @RequestBody UpdateMunicipalityStatusRequestDTO dto,
            HttpServletRequest request
    ) {
        logger.info("Updating status for municipality with ID: {}", municipalityId);
        Municipality municipality = municipalityRepository.findById(municipalityId)
                .orElseThrow(() -> new EntityNotFoundException("Municipality not found"));

        MunicipalityStatus status = municipalityStatusRepository.findById(dto.getMunicipalityStatusId())
                .orElseThrow(() -> new IllegalArgumentException("Municipality status not found"));

        municipality.setStatus(status);
        municipalityRepository.save(municipality);

        return new GlobalResponseHandler().handleResponse(
                "Municipality status updated successfully",
                municipality,
                HttpStatus.OK,
                request
        );
    }

    @PatchMapping("/{municipalityId}/responsible")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateResponsible(
            @PathVariable Long municipalityId,
            @RequestBody UpdateResponsibleRequestDTO dto,
            HttpServletRequest request
    ) {
        logger.info("Updating responsible for municipality with ID: {}", municipalityId);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<Municipality> municipality = municipalityRepository.findById(municipalityId);
        if (municipality.isEmpty()) {
            return globalResponseHandler.handleResponse(
                    "Municipality with ID " + municipalityId + " not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }

        municipality.get().setResponsibleName(dto.getResponsibleName());
        municipality.get().setResponsibleRole(dto.getResponsibleRole());
        municipalityRepository.save(municipality.get());

        return new GlobalResponseHandler().handleResponse(
                "Responsible updated successfully",
                municipality,
                HttpStatus.OK,
                request
        );
    }

    @PatchMapping("/{municipalityId}/logo")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateLogo(
            @PathVariable Long municipalityId,
            @RequestBody UpdateLogoRequestDTO dto,
            HttpServletRequest request
    ) {
        logger.info("Updating logo for municipality with ID: {}", municipalityId);
        var globalResponseHandler = new GlobalResponseHandler();
        Optional<Municipality> municipality = municipalityRepository.findById(municipalityId);
        if (municipality.isEmpty()) {
            return globalResponseHandler.notFound(
                    "Municipality with ID " + municipalityId + " not found",
                    request
            );
        }


        municipality.get().setLogo(dto.getLogo());
        municipalityRepository.save(municipality.get());

        return new GlobalResponseHandler().handleResponse(
                "Logo updated successfully",
                municipality,
                HttpStatus.OK,
                request
        );
    }

    @DeleteMapping("/{municipalityId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long municipalityId, HttpServletRequest request) {
        logger.info("Deleting municipality with ID: {}", municipalityId);
        var globalResponseHandler = new GlobalResponseHandler();
        Optional<Municipality> found = municipalityRepository.findById(municipalityId);
        if (found.isPresent()) {
            municipalityRepository.deleteById(municipalityId);
            return globalResponseHandler.success(
                    "Municipality deleted successfully",
                    found.get(),
                    request
            );
        } else {
            return globalResponseHandler.notFound(
                    "Municipality not found",
                    request
            );
        }
    }

    /**
     * Validates the municipality data to ensure no duplicates exist.
     *
     * @param name       the name of the municipality
     * @param email      the email of the municipality
     * @param cantonId   the ID of the canton
     * @param idActual   the current ID of the municipality being updated (null if creating)
     * @author dgutierrez
     */
        private void validarMunicipality(String name, String email, Long cantonId, Long idActual) {
        municipalityRepository.findByName(name).ifPresent(existing -> {
            if (!Objects.equals(existing.getId(), idActual)) {
                throw new IllegalArgumentException("A municipality with this name already exists");
            }
        });

        municipalityRepository.findByEmail(email).ifPresent(existing -> {
            if (!Objects.equals(existing.getId(), idActual)) {
                throw new IllegalArgumentException("A municipality with this email already exists");
            }
        });

        boolean cantonUsed = municipalityRepository.existsByCantonId(cantonId);
        if (cantonUsed && (idActual == null || municipalityRepository.findById(idActual)
                .map(m -> !m.getCanton().getId().equals(cantonId)).orElse(true))) {
            throw new IllegalArgumentException("A municipality for this canton already exists");
        }

        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.getMunicipality() == null || !Objects.equals(user.getMunicipality().getId(), idActual)) {
                throw new IllegalArgumentException("A user with this email already exists");
            }
        });
    }
}
