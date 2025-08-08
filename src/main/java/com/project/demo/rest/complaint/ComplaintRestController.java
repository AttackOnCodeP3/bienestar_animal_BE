package com.project.demo.rest.complaint;

import com.project.demo.logic.entity.auth.JwtService;
import com.project.demo.logic.entity.complaint.Complaint;
import com.project.demo.logic.entity.complaint.ComplaintRepository;
import com.project.demo.logic.entity.complaint_state.ComplaintStateEnum;
import com.project.demo.logic.entity.complaint_state.ComplaintStateRepository;
import com.project.demo.logic.entity.complaint_type.ComplaintTypeRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.notification.NotificationService;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.rest.complaint.dto.CreateComplaintMultipartDTO;
import com.project.demo.rest.complaint.dto.ObservationsDTO;
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
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/complaints")
public class ComplaintRestController {

    private static final Logger logger = LoggerFactory.getLogger(ComplaintRestController.class);

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ComplaintRepository complaintRepository;
    @Autowired
    private ComplaintTypeRepository complaintTypeRepository;
    @Autowired
    private ComplaintStateRepository complaintStateRepository;
    @Autowired
    private Tripo3DService tripo3DService;
    @Autowired
    private NotificationService notificationService;

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

        var complaintState = complaintStateRepository.findByName(ComplaintStateEnum.OPEN.getName());
        if (complaintState.isEmpty()) {
            return handler.internalError("No se encontró el estado inicial 'Abierta'", request);
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
                .complaintState(complaintState.get())
                .createdBy(optionalUser.get())
                .observations(null)
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

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MUNICIPAL_ADMIN')")
    @Transactional
    public ResponseEntity<?> approveComplaint(@PathVariable Long id, HttpServletRequest req) {
        var handler = new GlobalResponseHandler();
        var opt = complaintRepository.findById(id);
        if (opt.isEmpty()) return handler.notFound("Denuncia no encontrada", req);

        var complaint = opt.get();
        var currentState = getComplaintStateEnumFromEntity(complaint);

        if (currentState == null) {
            return handler.internalError("Estado de denuncia desconocido", req);
        }

        List<ComplaintStateEnum> nonApprovingStates = List.of(
                ComplaintStateEnum.COMPLETED,
                ComplaintStateEnum.APPROVED,
                ComplaintStateEnum.WITH_OBSERVATIONS
        );

        if (nonApprovingStates.contains(currentState)) {
            return handler.badRequest("No se puede aprobar una denuncia en estado: " + currentState.getName(), req);
        }

        var stateOpt = complaintStateRepository.findByName(ComplaintStateEnum.APPROVED.getName());
        if (stateOpt.isEmpty()) return handler.internalError("Estado Aprobada no encontrado", req);

        complaint.setComplaintState(stateOpt.get());
        complaint.setObservations(null);
        complaintRepository.save(complaint);

        notificationService.notifyComplaintStateChanged(complaint);
        return handler.handleResponse("Denuncia aprobada", complaint, HttpStatus.OK, req);
    }

    @PutMapping("/{id}/observe")
    @PreAuthorize("hasAnyRole('MUNICIPAL_ADMIN')")
    @Transactional
    public ResponseEntity<?> observeComplaint(@PathVariable Long id,
                                              @RequestBody ObservationsDTO dto,
                                              HttpServletRequest req) {
        var handler = new GlobalResponseHandler();
        var opt = complaintRepository.findById(id);
        if (opt.isEmpty()) return handler.notFound("Denuncia no encontrada", req);

        var c = opt.get();
        var currentState = getComplaintStateEnumFromEntity(c);

        var nonObservingStates = List.of(
                ComplaintStateEnum.COMPLETED,
                ComplaintStateEnum.APPROVED
        );

        if (nonObservingStates.contains(currentState)) {
            return handler.badRequest("No se puede indicar observaciones en una denuncia en estado: " + currentState.getName(), req);
        }

        var stateOpt = complaintStateRepository.findByName(ComplaintStateEnum.WITH_OBSERVATIONS.getName());
        if (stateOpt.isEmpty()) return handler.internalError("Estado Con observaciones no encontrado", req);

        c.setComplaintState(stateOpt.get());
        c.setObservations(dto.observations());
        complaintRepository.save(c);

        notificationService.notifyComplaintObservationsToUser(c);
        return handler.handleResponse("Observaciones agregadas y enviadas para su revisión", c, HttpStatus.OK, req);
    }

    @PutMapping("/{id}/resubmit")
    @PreAuthorize("hasAnyRole('COMMUNITY_USER')")
    @Transactional
    public ResponseEntity<?> resubmitComplaint(@PathVariable Long id, HttpServletRequest req) {
        var handler = new GlobalResponseHandler();
        var email = jwtService.extractUsername(jwtService.getTokenFromHeader(req.getHeader("Authorization")));
        var optUser = userRepository.findByEmail(email);
        var cOpt = complaintRepository.findById(id);

        if (optUser.isEmpty() || cOpt.isEmpty()) return handler.notFound("Usuario o denuncia no encontrada", req);

        var c = cOpt.get();
        if (!c.getCreatedBy().equals(optUser.get())) {
            return handler.badRequest("No puedes reenviar una denuncia que no es tuya", req);
        }

        var currentState = getComplaintStateEnumFromEntity(c);
        if (currentState != ComplaintStateEnum.WITH_OBSERVATIONS) {
            return handler.badRequest("Solo puedes reenviar si la denuncia está en estado 'Con observaciones'", req);
        }

        var stateOpt = complaintStateRepository.findByName(ComplaintStateEnum.OPEN.getName());
        if (stateOpt.isEmpty()) return handler.internalError("Estado Abierta no encontrado", req);

        c.setComplaintState(stateOpt.get());
        c.setObservations(null);
        complaintRepository.save(c);

        notificationService.notifyResubmission(c);
        return handler.handleResponse("Denuncia reenviada a estado abierta", c, HttpStatus.OK, req);
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('MUNICIPAL_ADMIN')")
    @Transactional
    public ResponseEntity<?> completeComplaint(@PathVariable Long id, HttpServletRequest req) {
        var handler = new GlobalResponseHandler();
        var cOpt = complaintRepository.findById(id);
        if (cOpt.isEmpty()) return handler.notFound("Denuncia no encontrada", req);

        var c = cOpt.get();
        var currentState = getComplaintStateEnumFromEntity(c);

        if (currentState != ComplaintStateEnum.APPROVED) {
            return handler.badRequest("Solo se puede completar una denuncia aprobada", req);
        }

        var stateOpt = complaintStateRepository.findByName(ComplaintStateEnum.COMPLETED.getName());
        if (stateOpt.isEmpty()) return handler.internalError("Estado Completada no encontrado", req);

        c.setComplaintState(stateOpt.get());
        complaintRepository.save(c);

        notificationService.notifyComplaintCompleted(c);
        return handler.handleResponse("Denuncia completada", c, HttpStatus.OK, req);
    }

    private ComplaintStateEnum getComplaintStateEnumFromEntity(Complaint complaint) {
        return Arrays.stream(ComplaintStateEnum.values())
                .filter(e -> e.getName().equalsIgnoreCase(complaint.getComplaintState().getName()))
                .findFirst()
                .orElse(null);
    }
}