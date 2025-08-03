package com.project.demo.rest.complaint;

import com.project.demo.logic.entity.complaint.Complaint;
import com.project.demo.logic.entity.complaint.ComplaintRepository;
import com.project.demo.logic.entity.complaint_type.ComplaintTypeRepository;
import com.project.demo.logic.entity.auth.JwtService;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.notification.NotificationService;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.rest.complaint.dto.CreateComplaintMultipartDTO;
import com.project.demo.service.model.Tripo3DService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@RestController
@RequestMapping("/complaints")
public class ComplaintRestController {

    private static final Logger logger = LoggerFactory.getLogger(ComplaintRestController.class);

    @Autowired private JwtService jwtService;
    @Autowired private UserRepository userRepository;
    @Autowired private ComplaintTypeRepository complaintTypeRepository;
    @Autowired private Tripo3DService tripo3DService;
    @Autowired private ComplaintRepository complaintRepository;
    @Autowired private NotificationService notificationService;

    /**
     * Creates a new complaint with an optional image file.
     * The user is inferred from the JWT token.
     *
     * @param authHeader JWT Authorization header.
     * @param dto        DTO with complaint data and optional image.
     * @param request    HTTP request for context and error responses.
     * @return ResponseEntity with complaint info or error.
     * @throws IOException if image upload fails.
     * @author dgutierrez
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('COMMUNITY_USER')")
    @Transactional
    public ResponseEntity<?> createComplaint(
            @RequestHeader("Authorization") String authHeader,
            @ModelAttribute CreateComplaintMultipartDTO dto,
            HttpServletRequest request
    ) throws IOException {
        logger.info("POST /complaints - Creando denuncia");

        var handler = new GlobalResponseHandler();

        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            return handler.badRequest("La descripción de la denuncia es obligatoria", request);
        }

        if (dto.getLatitude() == null || dto.getLongitude() == null) {
            return handler.badRequest("Las coordenadas geográficas son obligatorias", request);
        }

        var optionalType = complaintTypeRepository.findById(dto.getComplaintTypeId());
        if (optionalType.isEmpty()) {
            return handler.badRequest("El tipo de denuncia con ID " + dto.getComplaintTypeId() + " no fue encontrado", request);
        }

        String email = jwtService.extractUsername(jwtService.getTokenFromHeader(authHeader));
        var optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return handler.notFound("No se encontró el usuario autenticado", request);
        }

        String imageUrl = null;
        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            imageUrl = tripo3DService.uploadToImgur(dto.getImage());
        }

        var complaint = Complaint.builder()
                .description(dto.getDescription())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .imageUrl(imageUrl)
                .complaintType(optionalType.get())
                .createdBy(optionalUser.get())
                .build();

        complaintRepository.save(complaint);

        logger.info("Denuncia creada exitosamente: {}", complaint);

        notificationService.notifyComplaintCreationToAdministrators(
                null,
                complaint.getCreatedBy().getMunicipality().getId()
        );

        return handler.handleResponse(
                "Su denuncia ha sido recibida exitosamente, gracias por su colaboración.",
                complaint,
                HttpStatus.CREATED,
                request
        );
    }
}
