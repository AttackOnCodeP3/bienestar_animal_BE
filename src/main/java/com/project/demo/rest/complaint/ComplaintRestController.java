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
import com.project.demo.rest.complaint.dto.UpdateComplaintMultipartDTO;
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

    /* ============================================================
       CONSULTAS - ADMIN MUNICIPAL
       ============================================================ */

    /**
     * Filtra denuncias por tipo/estado dentro de la municipalidad del admin autenticado.
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
     * Obtiene una denuncia por ID si pertenece a la municipalidad del admin.
     * @author dgutierrez
     */
    @GetMapping("/my-municipality/{id}")
    @PreAuthorize("hasRole('MUNICIPAL_ADMIN')")
    public ResponseEntity<?> getComplaintByIdForMunicipalityAdmin(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest request
    ) {
        logger.info("GET /complaints/my-municipality/{} - Fetching complaint for municipal admin", id);
        var handler = new GlobalResponseHandler();

        var optionalComplaint = complaintRepository.findById(id);
        if (optionalComplaint.isEmpty()) {
            return handler.notFound("Denuncia no encontrada", request);
        }

        var complaint = optionalComplaint.get();
        if (isOutsideAdminMunicipality(complaint, authHeader)) {
            return handler.badRequest("La denuncia no pertenece a su municipalidad", request);
        }

        return wrapComplaintAsDto("Denuncia obtenida correctamente", complaint, HttpStatus.OK, request);
    }

    /* ============================================================
       CONSULTAS - USUARIO COMUNITARIO
       ============================================================ */

    /**
     * Denuncias del usuario autenticado (opcionalmente filtradas).
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
     * Obtiene una denuncia del usuario comunitario (propiedad obligatoria).
     * @author dgutierrez
     */
    @GetMapping("/my-complaints/{id}")
    @PreAuthorize("hasRole('COMMUNITY_USER')")
    public ResponseEntity<?> getComplaintByIdForCommunityUser(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest request
    ) {
        logger.info("GET /complaints/my-complaints/{} - Fetching complaint for community user", id);
        var handler = new GlobalResponseHandler();

        String email = jwtService.extractUsername(jwtService.getTokenFromHeader(authHeader));
        var optionalUser = userRepository.findByEmail(email);
        var optionalComplaint = complaintRepository.findById(id);

        if (optionalUser.isEmpty() || optionalComplaint.isEmpty()) {
            return handler.notFound("Usuario o denuncia no encontrada", request);
        }

        var user = optionalUser.get();
        var complaint = optionalComplaint.get();

        if (!complaint.getCreatedBy().getId().equals(user.getId())) {
            return handler.badRequest("No tienes permiso para acceder a esta denuncia", request);
        }

        return wrapComplaintAsDto("Denuncia obtenida correctamente", complaint, HttpStatus.OK, request);
    }

    /**
     * Crea una denuncia (estado inicial: Abierta).
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

    /* ============================================================
       ACCIONES - USUARIO COMUNITARIO
       ============================================================ */

    /**
     * Actualiza una denuncia del comunitario.
     * - Si está Abierta: solo actualiza campos.
     * - Si está Con observaciones: actualiza y pasa a Abierta (limpia observaciones).
     * - Otros estados: prohibido.
     * @author dgutierrez
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COMMUNITY_USER')")
    @Transactional
    public ResponseEntity<?> updateComplaint(@PathVariable Long id,
                                             @ModelAttribute UpdateComplaintMultipartDTO dto,
                                             @RequestHeader("Authorization") String authHeader,
                                             HttpServletRequest req) throws IOException {
        var handler = new GlobalResponseHandler();

        String email = jwtService.extractUsername(jwtService.getTokenFromHeader(authHeader));
        var optUser = userRepository.findByEmail(email);
        var opt = complaintRepository.findById(id);
        if (optUser.isEmpty() || opt.isEmpty()) return handler.notFound("Usuario o denuncia no encontrada", req);

        var c = opt.get();
        if (!c.getCreatedBy().getId().equals(optUser.get().getId())) {
            return handler.badRequest("No puedes actualizar una denuncia que no es tuya", req);
        }

        var state = getComplaintStateEnumFromEntity(c);
        if (!(state == ComplaintStateEnum.OPEN || state == ComplaintStateEnum.WITH_OBSERVATIONS)) {
            return handler.badRequest("Solo se puede actualizar si está Abierta o Con observaciones", req);
        }

        if (dto.description() != null) c.setDescription(dto.description());
        if (dto.latitude() != null)   c.setLatitude(dto.latitude());
        if (dto.longitude() != null)  c.setLongitude(dto.longitude());
        if (dto.complaintTypeId() != null) {
            var optType = complaintTypeRepository.findById(dto.complaintTypeId());
            if (optType.isEmpty()) return handler.badRequest("Tipo de denuncia inválido", req);
            c.setComplaintType(optType.get());
        }
        if (dto.image() != null && !dto.image().isEmpty()) {
            c.setImageUrl(tripo3DService.uploadToImgur(dto.image()));
        }

        // Si estaba Con observaciones, reabre
        if (state == ComplaintStateEnum.WITH_OBSERVATIONS) {
            var open = complaintStateRepository.findByName(ComplaintStateEnum.OPEN.getName());
            if (open.isEmpty()) return handler.internalError("Estado Abierta no encontrado", req);
            c.setComplaintState(open.get());
            complaintRepository.save(c);
            notificationService.notifyResubmission(c);
            return wrapComplaintAsDto("Denuncia actualizada y reenviada a estado Abierta", c, HttpStatus.OK, req);
        }

        complaintRepository.save(c);
        return wrapComplaintAsDto("Denuncia actualizada", c, HttpStatus.OK, req);
    }

    /**
     * Cancela una denuncia del comunitario (solo si está Abierta).
     * Solo el usuario comunitario que la creó puede cancelarla.
     * @author dgutierrez
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('COMMUNITY_USER')")
    @Transactional
    public ResponseEntity<?> cancelComplaint(@PathVariable Long id,
                                             @RequestHeader("Authorization") String authHeader,
                                             HttpServletRequest req) {
        var handler = new GlobalResponseHandler();

        String email = jwtService.extractUsername(jwtService.getTokenFromHeader(authHeader));
        var optUser = userRepository.findByEmail(email);
        var opt = complaintRepository.findById(id);
        if (optUser.isEmpty() || opt.isEmpty()) return handler.notFound("Usuario o denuncia no encontrada", req);

        var c = opt.get();
        if (!c.getCreatedBy().getId().equals(optUser.get().getId())) {
            return handler.badRequest("No puedes cancelar una denuncia que no es tuya", req);
        }

        var state = getComplaintStateEnumFromEntity(c);
        if (state != ComplaintStateEnum.OPEN) {
            return handler.badRequest("Solo se puede cancelar una denuncia en estado Abierta", req);
        }

        var cancel = complaintStateRepository.findByName(ComplaintStateEnum.CANCELLED.getName());
        if (cancel.isEmpty()) return handler.internalError("Estado Cancelada no encontrado", req);

        c.setComplaintState(cancel.get());
        complaintRepository.save(c);

        return wrapComplaintAsDto("Denuncia cancelada", c, HttpStatus.OK, req);
    }

    /**
     * Cancela una denuncia como administrador municipal.
     * Puede cancelar en estado Abierta, Con observaciones o Aprobada.
     * @author dgutierrez
     */
    @PutMapping("/{id}/cancel/admin")
    @PreAuthorize("hasRole('MUNICIPAL_ADMIN')")
    @Transactional
    public ResponseEntity<?> cancelComplaintAsAdmin(@PathVariable Long id,
                                                    @RequestHeader("Authorization") String authHeader,
                                                    HttpServletRequest req) {
        var handler = new GlobalResponseHandler();
        var opt = complaintRepository.findById(id);
        if (opt.isEmpty()) return handler.notFound("Denuncia no encontrada", req);

        var c = opt.get();
        if (isOutsideAdminMunicipality(c, authHeader)) {
            return handler.badRequest("La denuncia no pertenece a su municipalidad", req);
        }

        var state = getComplaintStateEnumFromEntity(c);
        if (!(state == ComplaintStateEnum.OPEN
                || state == ComplaintStateEnum.WITH_OBSERVATIONS
                || state == ComplaintStateEnum.APPROVED)) {
            return handler.badRequest("Solo se puede cancelar una denuncia en estado Abierta, Con observaciones o Aprobada", req);
        }

        var cancel = complaintStateRepository.findByName(ComplaintStateEnum.CANCELLED.getName());
        if (cancel.isEmpty()) return handler.internalError("Estado Cancelada no encontrado", req);

        c.setComplaintState(cancel.get());
        if (c.getObservations() == null || c.getObservations().isBlank()) {
            c.setObservations("Cancelada por la municipalidad.");
        }
        complaintRepository.save(c);

        return wrapComplaintAsDto("Denuncia cancelada por la municipalidad", c, HttpStatus.OK, req);
    }

    /**
     * Reenvía una denuncia que está Con observaciones → Abierta (atajo explícito).
     * Solo el usuario comunitario que la creó puede reenviarla.
     * @author dgutierrez
     */
    @PutMapping("/{id}/resubmit")
    @PreAuthorize("hasAnyRole('COMMUNITY_USER')")
    @Transactional
    public ResponseEntity<?> resubmitComplaint(@PathVariable Long id,
                                               @RequestHeader("Authorization") String authHeader,
                                               HttpServletRequest req) {
        var handler = new GlobalResponseHandler();
        var email = jwtService.extractUsername(jwtService.getTokenFromHeader(authHeader));
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
        complaintRepository.save(c);

        notificationService.notifyResubmission(c);
        return wrapComplaintAsDto("Denuncia reenviada a estado Abierta", c, HttpStatus.OK, req);
    }

    /* ============================================================
       ACCIONES - ADMIN MUNICIPAL
       ============================================================ */

    /**
     * Pasa a Con observaciones (o reescribe observaciones) desde Abierta/Con observaciones.
     * Solo el admin municipal puede agregar observaciones.
     * @author dgutierrez
     */
    @PutMapping("/{id}/observe")
    @PreAuthorize("hasAnyRole('MUNICIPAL_ADMIN')")
    @Transactional
    public ResponseEntity<?> observeComplaint(@PathVariable Long id,
                                              @RequestBody ObservationsDTO dto,
                                              @RequestHeader("Authorization") String authHeader,
                                              HttpServletRequest req) {
        var handler = new GlobalResponseHandler();
        var opt = complaintRepository.findById(id);
        if (opt.isEmpty()) return handler.notFound("Denuncia no encontrada", req);

        var c = opt.get();
        if (isOutsideAdminMunicipality(c, authHeader)) {
            return handler.badRequest("La denuncia no pertenece a su municipalidad", req);
        }

        var state = getComplaintStateEnumFromEntity(c);
        if (!(state == ComplaintStateEnum.OPEN || state == ComplaintStateEnum.WITH_OBSERVATIONS)) {
            return handler.badRequest("No se pueden indicar observaciones en el estado actual", req);
        }

        var next = complaintStateRepository.findByName(ComplaintStateEnum.WITH_OBSERVATIONS.getName());
        if (next.isEmpty()) return handler.internalError("Estado Con observaciones no encontrado", req);

        c.setComplaintState(next.get());
        c.setObservations(dto.observations());
        complaintRepository.save(c);

        notificationService.notifyComplaintObservationsToUser(c);
        return wrapComplaintAsDto("Observaciones agregadas", c, HttpStatus.OK, req);
    }

    /**
     * Aprueba una denuncia (solo si está Abierta).
     * Solo el admin municipal puede aprobar denuncias.
     * @author dgutierrez
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MUNICIPAL_ADMIN')")
    @Transactional
    public ResponseEntity<?> approveComplaint(@PathVariable Long id,
                                              @RequestHeader("Authorization") String authHeader,
                                              HttpServletRequest req) {
        var handler = new GlobalResponseHandler();
        var opt = complaintRepository.findById(id);
        if (opt.isEmpty()) return handler.notFound("Denuncia no encontrada", req);

        var c = opt.get();
        if (isOutsideAdminMunicipality(c, authHeader)) {
            return handler.badRequest("La denuncia no pertenece a su municipalidad", req);
        }

        var currentState = getComplaintStateEnumFromEntity(c);
        if (currentState != ComplaintStateEnum.OPEN) {
            return handler.badRequest("Solo se puede aprobar una denuncia en estado Abierta", req);
        }

        var stateOpt = complaintStateRepository.findByName(ComplaintStateEnum.APPROVED.getName());
        if (stateOpt.isEmpty()) return handler.internalError("Estado Aprobada no encontrado", req);

        c.setComplaintState(stateOpt.get());
        complaintRepository.save(c);

        notificationService.notifyComplaintStateChanged(c);
        return wrapComplaintAsDto("Denuncia aprobada", c, HttpStatus.OK, req);
    }

    /**
     * Completa una denuncia (solo si está Aprobada).
     * Solo el admin municipal puede completar denuncias.
     * @author dgutierrez
     */
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('MUNICIPAL_ADMIN')")
    @Transactional
    public ResponseEntity<?> completeComplaint(@PathVariable Long id,
                                               @RequestHeader("Authorization") String authHeader,
                                               HttpServletRequest req) {
        var handler = new GlobalResponseHandler();
        var cOpt = complaintRepository.findById(id);
        if (cOpt.isEmpty()) return handler.notFound("Denuncia no encontrada", req);

        var c = cOpt.get();
        if (isOutsideAdminMunicipality(c, authHeader)) {
            return handler.badRequest("La denuncia no pertenece a su municipalidad", req);
        }

        var currentState = getComplaintStateEnumFromEntity(c);
        if (currentState != ComplaintStateEnum.APPROVED) {
            return handler.badRequest("Solo se puede completar una denuncia Aprobada", req);
        }

        var stateOpt = complaintStateRepository.findByName(ComplaintStateEnum.COMPLETED.getName());
        if (stateOpt.isEmpty()) return handler.internalError("Estado Completada no encontrado", req);

        c.setComplaintState(stateOpt.get());
        complaintRepository.save(c);

        notificationService.notifyComplaintCompleted(c);
        return wrapComplaintAsDto("Denuncia completada", c, HttpStatus.OK, req);
    }

    /**
     * Cierra una denuncia.
     * Permitido si está Completada o Con observaciones (caso de abandono por el usuario).
     * Si está Abierta, se cierra con observaciones por defecto.
     * @author dgutierrez
     */
    @PutMapping("/{id}/close")
    @PreAuthorize("hasRole('MUNICIPAL_ADMIN')")
    @Transactional
    public ResponseEntity<?> closeComplaint(@PathVariable Long id,
                                            @RequestHeader("Authorization") String authHeader,
                                            HttpServletRequest req) {
        var handler = new GlobalResponseHandler();
        var opt = complaintRepository.findById(id);
        if (opt.isEmpty()) return handler.notFound("Denuncia no encontrada", req);

        var c = opt.get();
        if (isOutsideAdminMunicipality(c, authHeader)) {
            return handler.badRequest("La denuncia no pertenece a su municipalidad", req);
        }

        var state = getComplaintStateEnumFromEntity(c);

        if (state == ComplaintStateEnum.CANCELLED || state == ComplaintStateEnum.CLOSED) {
            return handler.badRequest("La denuncia ya se encuentra en un estado terminal", req);
        }

        var closed = complaintStateRepository.findByName(ComplaintStateEnum.CLOSED.getName());
        if (closed.isEmpty()) return handler.internalError("Estado Cerrada no encontrado", req);

        c.setComplaintState(closed.get());

        // mensaje/observación por defecto si venía de WITH_OBSERVATIONS o OPEN
        if ((state == ComplaintStateEnum.WITH_OBSERVATIONS || state == ComplaintStateEnum.OPEN)
                && (c.getObservations() == null || c.getObservations().isBlank())) {
            c.setObservations("Cerrada por la municipalidad.");
        }

        complaintRepository.save(c);
        return wrapComplaintAsDto("Denuncia cerrada", c, HttpStatus.OK, req);
    }

    /* ============================================================
       HELPERS
       ============================================================ */

    /**
     * Obtiene el estado de la denuncia como ComplaintStateEnum a partir de la entidad Complaint.
     * @param complaint
     * @return
     * @author dgutierrez
     */
    private ComplaintStateEnum getComplaintStateEnumFromEntity(Complaint complaint) {
        return Arrays.stream(ComplaintStateEnum.values())
                .filter(e -> e.getName().equalsIgnoreCase(complaint.getComplaintState().getName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Verifica si el estado es terminal (Cancelada o Cerrada)
     * @param s
     * @return
     * @author dgutierrez
     */
    private boolean isTerminal(ComplaintStateEnum s) {
        return s == ComplaintStateEnum.CANCELLED || s == ComplaintStateEnum.CLOSED;
    }

    /**
     * Verifica si la denuncia está fuera de la municipalidad del admin autenticado.
     * @param c Denuncia a verificar
     * @param authHeader Header de autorización del admin
     * @return true si está fuera, false si pertenece a su municipalidad
     */
    private boolean isOutsideAdminMunicipality(Complaint c, String authHeader) {
        String email = jwtService.extractUsername(jwtService.getTokenFromHeader(authHeader));
        return userRepository.findByEmail(email)
                .map(User::getMunicipality)
                .map(m -> !c.getCreatedBy().getMunicipality().getId().equals(m.getId()))
                .orElse(true);
    }

    /**
     * Envuelve una denuncia como DTO y la retorna en una ResponseEntity.
     * @param message Mensaje de éxito
     * @param complaint Denuncia a envolver
     * @param status Estado HTTP de la respuesta
     * @param request Objeto HttpServletRequest para el manejo de la respuesta
     * @return ResponseEntity con el DTO de la denuncia
     */
    private ResponseEntity<?> wrapComplaintAsDto(String message, Complaint complaint, HttpStatus status, HttpServletRequest request) {
        ComplaintDTO dto = ComplaintDTO.fromEntity(complaint);
        return new GlobalResponseHandler().handleResponse(message, dto, status, request);
    }
}
