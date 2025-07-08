package com.project.demo.rest.municipality;

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
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long cantonId,
            @RequestParam(required = false) MunicipalityStatusEnum status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
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

    @GetMapping("/{municipalityId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getById(@PathVariable Long municipalityId, HttpServletRequest request) {
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
    public ResponseEntity<?> create(@RequestBody CreateMunicipalityRequestDTO requestDTO, HttpServletRequest request) {
        boolean exists = municipalityRepository.findAll().stream()
                .anyMatch(m -> m.getCanton().getId().equals(requestDTO.getCantonId()));
        if (exists) {
            return new GlobalResponseHandler().handleResponse(
                    "A municipality for this canton already exists",
                    HttpStatus.BAD_REQUEST,
                    request
            );
        }

        Municipality municipality = Municipality.builder()
                .name(requestDTO.getName())
                .address(requestDTO.getAddress())
                .phone(requestDTO.getPhone())
                .email(requestDTO.getEmail())
                .responsibleName(requestDTO.getResponsibleName())
                .responsibleRole(requestDTO.getResponsiblePosition())
                .status(MunicipalityStatusEnum.ACTIVE)
                .canton(cantonRepository.findById(requestDTO.getCantonId())
                        .orElseThrow(() -> new IllegalArgumentException("Canton not found")))
                .build();

        Municipality savedMunicipality = municipalityRepository.save(municipality);

        User adminUser = new User();
        adminUser.setName(requestDTO.getResponsibleName());
        adminUser.setLastname(requestDTO.getResponsiblePosition());
        adminUser.setEmail(requestDTO.getEmail());
        adminUser.setPassword(passwordEncoder.encode("123")); // contraseña por defecto
        adminUser.setRequiresPasswordChange(true);
        adminUser.setMunicipality(savedMunicipality);

        Role adminRole = roleRepository.findByName(RoleEnum.MUNICIPAL_ADMIN)
                .orElseThrow(() -> new RuntimeException("MUNICIPAL_ADMIN role not found"));
        adminUser.addRole(adminRole);

        userRepository.save(adminUser);

        return new GlobalResponseHandler().handleResponse(
                "Municipality and admin user created successfully",
                savedMunicipality,
                HttpStatus.CREATED,
                request
        );
    }

    @PutMapping("/{municipalityId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long municipalityId, @RequestBody Municipality municipality, HttpServletRequest request) {
        Optional<Municipality> found = municipalityRepository.findById(municipalityId);
        if (found.isPresent()) {
            municipality.setId(municipalityId);
            Municipality updated = municipalityRepository.save(municipality);
            return new GlobalResponseHandler().handleResponse(
                    "Municipality updated successfully",
                    updated,
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

    @PatchMapping("/{municipalityId}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long municipalityId,
            @RequestBody UpdateMunicipalityStatusRequestDTO requestDTO,
            HttpServletRequest request
    ) {
        Optional<Municipality> optional = municipalityRepository.findById(municipalityId);
        if (optional.isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "Municipality not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }

        Municipality municipality = optional.get();
        municipality.setStatus(requestDTO.getStatus());
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
            @RequestBody UpdateResponsibleRequestDTO requestDTO,
            HttpServletRequest request
    ) {
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

    @PatchMapping("/{municipalityId}/logo")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateLogo(
            @PathVariable Long municipalityId,
            @RequestBody UpdateLogoRequestDTO requestDTO,
            HttpServletRequest request
    ) {
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

    @DeleteMapping("/{municipalityId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long municipalityId, HttpServletRequest request) {
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
