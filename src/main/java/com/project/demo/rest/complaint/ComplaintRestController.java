package com.project.demo.rest.complaint;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.auth.JwtService;
import com.project.demo.logic.entity.complaint.Complaint;
import com.project.demo.logic.entity.complaint.ComplaintRepository;
import com.project.demo.logic.entity.complaint_state.ComplaintStateEnum;
import com.project.demo.logic.entity.complaint_state.ComplaintStateRepository;
import com.project.demo.logic.entity.complaint_type.ComplaintTypeRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.notification.NotificationService;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.rest.complaint.dto.ComplaintDTO;
import com.project.demo.rest.complaint.dto.CreateComplaintMultipartDTO;
import com.project.demo.rest.complaint.dto.ObservationsDTO;
import com.project.demo.service.model.Tripo3DService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/complaints")
public class ComplaintRestController {

    private static final Logger logger = LoggerFactory.getLogger(ComplaintRestController.class);

    @Autowired private JwtService jwtService;
    @Autowired private UserRepository userRepository;
    @Autowired private ComplaintRepository complaintRepository;
    @Autowired private ComplaintTypeRepository complaintTypeRepository;
    @Autowired private ComplaintStateRepository complaintStateRepository;
    @Autowired private Tripo3DService tripo3DService;
    @Autowired private NotificationService notificationService;

    /**
     * Endpoint to filter complaints by type and state for the authenticated municipal admin's municipality.
     * Supports pagination and optional filtering parameters.
     * @author dgutierrez
     */
    @GetMapping("/my-municipality/filter")
    @PreAuthorize("hasRole('MUNICIPAL_ADMIN')")
    public ResponseEntity<?> filterComplaintsByTypeAndState(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) Long stateId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        logger.info("Invocando filterComplaintsByTypeAndState - typeId: {}, stateId: {}", typeId, stateId);

        String email = jwtService.extractUsername(jwtService.getTokenFromHeader(authHeader));
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty() || optionalUser.get().getMunicipality() == null) {
            return new GlobalResponseHandler().notFound(
                    "No se encontró la municipalidad del usuario autenticado",
                    request
            );
        }

        Long municipalityId = optionalUser.get().getMunicipality().getId();
        Pageable pageable = PaginationUtils.buildPageable(page, size);

        Page<Complaint> pageResult = complaintRepository
                .findByMunicipalityAndOptionalFilters(municipalityId, typeId, stateId, pageable);

        List<ComplaintDTO> dtoList = pageResult.getContent()
                .stream()
                .map(ComplaintDTO::fromEntity)
                .collect(Collectors.toList());

        Meta meta = PaginationUtils.buildMeta(request, pageResult);

        return new GlobalResponseHandler().handleResponse(
                "Denuncias filtradas obtenidas correctamente",
                dtoList,
                HttpStatus.OK,
                meta
        );
    }

    /**
     * Retrieves complaints created by the authenticated COMMUNITY_USER.
     * Supports optional filtering by complaint type and state, and is paginated.
     *
     * This endpoint is protected and only accessible by users with the COMMUNITY_USER role.
     *
     * @param authHeader JWT Authorization header.
     * @param typeId Optional complaint type filter.
     * @param stateId Optional complaint state filter.
     * @param page Page number for pagination (default is 1).
     * @param size Page size for pagination (default is 10).
     * @param request HTTP request metadata.
     * @return Paginated list of complaints created by the user.
     * @author dgutierrez
     */
    @GetMapping("/my-complaints")
    @PreAuthorize("hasRole('COMMUNITY_USER')")
    public ResponseEntity<?> getComplaintsByAuthenticatedCommunityUser(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) Long stateId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        logger.info("GET /complaints/my-complaints - typeId: {}, stateId: {}", typeId, stateId);
        var handler = new GlobalResponseHandler();

        String email = jwtService.extractUsername(jwtService.getTokenFromHeader(authHeader));
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return handler.notFound("No se encontró el usuario autenticado", request);
        }

        User user = optionalUser.get();
        Pageable pageable = PaginationUtils.buildPageable(page, size);

        Page<Complaint> pageResult = complaintRepository.findByUserAndOptionalFilters(
                user.getId(), typeId, stateId, pageable
        );

        List<ComplaintDTO> dtoList = pageResult.getContent()
                .stream()
                .map(ComplaintDTO::fromEntity)
                .collect(Collectors.toList());

        Meta meta = PaginationUtils.buildMeta(request, pageResult);

        return handler.handleResponse(
                "Denuncias del usuario obtenidas correctamente",
                dtoList,
                HttpStatus.OK,
                meta
        );
    }

    /**
     * Endpoint to create a new complaint. Only community users can create complaints.
     * Validates required fields and uploads an image if provided.
     * Sets the initial state of the complaint to "Open".
     * Notifies municipal administrators about the new complaint.
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

        return wrapComplaintAsDto(
                "Su denuncia ha sido recibida exitosamente, gracias por su colaboración.",
                complaint,
                HttpStatus.CREATED,
                request
        );
    }

    /**
     * Endpoint to approve a complaint. Only municipal admins can approve complaints.
     * Complaints can be approved if they are not in "Completed", "Approved", or "With Observations" states.
     * Upon approval, the complaint state is set to "Approved" and any existing observations are cleared.
     * Notifies the original complainant about the approval.
     * @author dgutierrez
     */
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
        return wrapComplaintAsDto("Denuncia aprobada", complaint, HttpStatus.OK, req);
    }

    /**
     * Endpoint to add observations to a complaint. Only municipal admins can add observations.
     * Observations can be added if the complaint is not in "Completed" or "Approved" states.
     * Upon adding observations, the complaint state is set to "With Observations".
     * Notifies the original complainant about the observations.
     * @author dgutierrez
     */
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
        return wrapComplaintAsDto("Observaciones agregadas y enviadas para su revisión", c, HttpStatus.OK, req);
    }

    /**
     * Endpoint to resubmit a complaint that has been returned with observations.
     * Only the original complainant can resubmit, and only if the complaint is in the "With Observations" state.
     * Upon resubmission, the complaint state is set back to "Open".
     * Notifies municipal administrators of the resubmission.
     * @author dgutierrez
     */
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
        return wrapComplaintAsDto("Denuncia reenviada a estado abierta", c, HttpStatus.OK, req);
    }

    /**
     * Endpoint to mark a complaint as completed. Only complaints in the "Approved" state can be completed.
     * Notifies the original complainant upon completion.
     * @author dgutierrez
     */
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
        return wrapComplaintAsDto("Denuncia completada", c, HttpStatus.OK, req);
    }

    /**
     * Helper method to map a Complaint entity's state to the corresponding ComplaintStateEnum.
     * @author dgutierrez
     */
    private ComplaintStateEnum getComplaintStateEnumFromEntity(Complaint complaint) {
        return Arrays.stream(ComplaintStateEnum.values())
                .filter(e -> e.getName().equalsIgnoreCase(complaint.getComplaintState().getName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Helper method to wrap a Complaint entity into a ComplaintDTO and return a standardized ResponseEntity.
     * @author dgutierrez
     */
    private ResponseEntity<?> wrapComplaintAsDto(String message, Complaint complaint, HttpStatus status, HttpServletRequest request) {
        ComplaintDTO dto = ComplaintDTO.fromEntity(complaint);
        return new GlobalResponseHandler().handleResponse(message, dto, status, request);
    }
}