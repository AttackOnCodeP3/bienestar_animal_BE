package com.project.demo.rest.announcement;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.announcement.Announcement;
import com.project.demo.logic.entity.announcement.AnnouncementRepository;
import com.project.demo.logic.entity.announcement_state.AnnouncementStateRepository;
import com.project.demo.logic.entity.auth.JwtService;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.notification.NotificationService;
import com.project.demo.logic.entity.notification_type.NotificationTypeEnum;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.rest.announcement.dto.CreateAnnouncementMultipartDTO;
import com.project.demo.rest.announcement.dto.UpdateAnnouncementMultipartDTO;
import com.project.demo.service.model.Tripo3DService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/announcements")
public class AnnouncementRestController {

    private static final Logger logger = LoggerFactory.getLogger(AnnouncementRestController.class);

    @Autowired private AnnouncementRepository announcementRepository;
    @Autowired private AnnouncementStateRepository announcementStateRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtService jwtService;
    @Autowired private Tripo3DService tripo3DService;
    @Autowired private NotificationService notificationService;

    /**
     * Retrieves all announcements in the system.
     * Accessible only by users with the SUPER_ADMIN role.
     *
     * @param page    the requested page number.
     * @param size    the number of items per page.
     * @param request the HTTP request for metadata construction.
     * @return a paginated list of all announcements.
     * @author dgutierrez
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> getAllAnnouncements(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        logger.info("Invocando getAllAnnouncements - Página {}, Tamaño {}", page, size);

        Pageable pageable = PaginationUtils.buildPageable(page, size);
        Page<Announcement> announcements = announcementRepository.findAll(pageable);

        Meta meta = PaginationUtils.buildMeta(request, announcements);

        return new GlobalResponseHandler().handleResponse(
                "Anuncios obtenidos correctamente",
                announcements.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    /**
     * Retrieves a specific announcement by its ID.
     * Accessible only by users with the SUPER_ADMIN role.
     *
     * @param id      the ID of the announcement.
     * @param request the HTTP request for error handling.
     * @return the announcement if found, otherwise a 404 response.
     * @author dgutierrez
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getById(@PathVariable Long id, HttpServletRequest request) {
        logger.info("Invocando getById - ID: {}", id);

        Optional<Announcement> optional = announcementRepository.findById(id);
        if (optional.isEmpty()) {
            return new GlobalResponseHandler().notFound(
                    "No se encontró el anuncio con ID " + id,
                    request
            );
        }

        return new GlobalResponseHandler().handleResponse(
                "Anuncio obtenido correctamente",
                optional.get(),
                HttpStatus.OK,
                request
        );
    }

    /**
     * Retrieves an announcement by its ID only if it belongs to the municipality
     * associated with the currently authenticated MUNICIPAL_ADMIN user.
     *
     * @param authHeader the Authorization header containing the JWT token.
     * @param id         the ID of the announcement.
     * @param request    the HTTP request for error handling.
     * @return the announcement if found within the user's municipality, or a 404 response.
     * @author dgutierrez
     */
    @GetMapping("/my-municipality/{id}")
    @PreAuthorize("hasRole('MUNICIPAL_ADMIN')")
    public ResponseEntity<?> getMyMunicipalityAnnouncementById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        logger.info("Invocando getMyMunicipalityAnnouncementById - ID: {}", id);

        String email = jwtService.extractUsername(jwtService.getTokenFromHeader(authHeader));

        var optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty() || optionalUser.get().getMunicipality() == null) {
            return new GlobalResponseHandler().notFound(
                    "No se encontró la municipalidad del usuario autenticado",
                    request
            );
        }

        Long municipalityId = optionalUser.get().getMunicipality().getId();
        Optional<Announcement> optional = announcementRepository.findByIdAndMunicipalities_Id(id, municipalityId);

        if (optional.isEmpty()) {
            return new GlobalResponseHandler().notFound(
                    "No se encontró el anuncio con ID " + id + " para la municipalidad del usuario",
                    request
            );
        }

        return new GlobalResponseHandler().handleResponse(
                "Anuncio de la municipalidad obtenido correctamente",
                optional.get(),
                HttpStatus.OK,
                request
        );
    }

    /**
     * Retrieves all announcements filtered by a given announcement state ID.
     * Accessible only by users with the SUPER_ADMIN role.
     *
     * @param stateId the ID of the announcement state.
     * @param page    the requested page number.
     * @param size    the number of items per page.
     * @param request the HTTP request for metadata and error handling.
     * @return a paginated list of announcements with the given state.
     * @author dgutierrez
     */
    @GetMapping("/by-state/{stateId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> getByState(
            @PathVariable Long stateId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        logger.info("Invocando getByState - Estado ID: {}, Página: {}, Tamaño: {}", stateId, page, size);

        if (!announcementStateRepository.existsById(stateId)) {
            return new GlobalResponseHandler().notFound(
                    "El estado de anuncio con ID " + stateId + " no fue encontrado",
                    request
            );
        }

        Pageable pageable = PaginationUtils.buildPageable(page, size);
        Page<Announcement> pageResult = announcementRepository.findByState_Id(stateId, pageable);

        Meta meta = PaginationUtils.buildMeta(request, pageResult);

        return new GlobalResponseHandler().handleResponse(
                "Anuncios filtrados por estado obtenidos correctamente",
                pageResult.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    /**
     * Retrieves announcements that belong to the currently authenticated user's municipality
     * and are filtered by a given announcement state ID.
     *
     * @param authHeader the Authorization header containing the JWT token.
     * @param stateId    the ID of the announcement state.
     * @param page       the requested page number.
     * @param size       the number of items per page.
     * @param request    the HTTP request for metadata and error handling.
     * @return a paginated list of announcements for the municipality and state.
     * @author dgutierrez
     */
    @GetMapping("/my-municipality/by-state/{stateId}")
    @PreAuthorize("hasRole('MUNICIPAL_ADMIN')")
    public ResponseEntity<?> getMyMunicipalityAnnouncementsByState(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long stateId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        logger.info("Invocando getMyMunicipalityAnnouncementsByState - Estado ID: {}", stateId);

        if (!announcementStateRepository.existsById(stateId)) {
            return new GlobalResponseHandler().notFound(
                    "El estado de anuncio con ID " + stateId + " no fue encontrado",
                    request
            );
        }

        String email = jwtService.extractUsername(jwtService.getTokenFromHeader(authHeader));
        var optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty() || optionalUser.get().getMunicipality() == null) {
            return new GlobalResponseHandler().notFound(
                    "No se encontró la municipalidad del usuario autenticado",
                    request
            );
        }

        Long municipalityId = optionalUser.get().getMunicipality().getId();
        Pageable pageable = PaginationUtils.buildPageable(page, size);
        Page<Announcement> pageResult = announcementRepository.findByState_IdAndMunicipalities_Id(stateId, municipalityId, pageable);
        Meta meta = PaginationUtils.buildMeta(request, pageResult);

        return new GlobalResponseHandler().handleResponse(
                "Anuncios filtrados por estado y municipalidad obtenidos correctamente",
                pageResult.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    /**
     * Retrieves all announcements associated with the municipality of the currently
     * authenticated MUNICIPAL_ADMIN user.
     *
     * @param authHeader the Authorization header containing the JWT token.
     * @param page       the requested page number.
     * @param size       the number of items per page.
     * @param request    the HTTP request for metadata construction.
     * @return a paginated list of announcements belonging to the user's municipality.
     * @author dgutierrez
     */
    @GetMapping("/my-municipality")
    @PreAuthorize("hasAnyRole('MUNICIPAL_ADMIN')")
    public ResponseEntity<?> getAnnouncementsForMyMunicipality(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        logger.info("Invocando getAnnouncementsForMyMunicipality");

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

        Page<Announcement> pageResult = announcementRepository.findByMunicipalities_Id(municipalityId, pageable);
        Meta meta = PaginationUtils.buildMeta(request, pageResult);

        return new GlobalResponseHandler().handleResponse(
                "Anuncios de la municipalidad del usuario obtenidos correctamente",
                pageResult.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    /**
     * Retrieves announcements for the authenticated MUNICIPAL_ADMIN user
     * filtered optionally by title and state ID.
     *
     * @param authHeader Authorization header with JWT.
     * @param title      (optional) Partial or full title of the announcement.
     * @param stateId    (optional) ID of the announcement state.
     * @param page       Page number for pagination.
     * @param size       Page size for pagination.
     * @param request    HTTP request for metadata.
     * @return A paginated list of filtered announcements.
     */
    @GetMapping("/my-municipality/filter")
    @PreAuthorize("hasRole('MUNICIPAL_ADMIN')")
    public ResponseEntity<?> filterAnnouncementsByTitleAndState(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long stateId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        logger.info("Invocando filterAnnouncementsByTitleAndState - title: {}, stateId: {}", title, stateId);

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

        Page<Announcement> pageResult = announcementRepository
                .findByMunicipalityAndOptionalFilters(municipalityId, title, stateId, pageable);

        Meta meta = PaginationUtils.buildMeta(request, pageResult);

        return new GlobalResponseHandler().handleResponse(
                "Anuncios filtrados obtenidos correctamente",
                pageResult.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    /**
     * Creates a new announcement associated with the currently authenticated MUNICIPAL_ADMIN's municipality.
     * The announcement requires an image file and metadata sent as multipart/form-data.
     *
     * @param authHeader Authorization header with JWT.
     * @param dto        DTO containing the announcement metadata and image.
     * @param request    HTTP request for error handling and metadata.
     * @return ResponseEntity with created announcement or error message.
     * @throws IOException if an error occurs during image upload.
     * @author dgutierrez
     */
    @PostMapping("/my-municipality")
    @PreAuthorize("hasRole('MUNICIPAL_ADMIN')")
    @Transactional
    public ResponseEntity<?> createAnnouncementForMyMunicipality(
            @RequestHeader("Authorization") String authHeader,
            @ModelAttribute CreateAnnouncementMultipartDTO dto,
            HttpServletRequest request
    ) throws IOException {
        logger.info("POST /announcements/my-municipality - Creando anuncio para el usuario autenticado administrador municipal");

        var globalResponseHandler = new GlobalResponseHandler();

        if (dto.getFile() == null || dto.getFile().isEmpty()) {
            return globalResponseHandler.badRequest(
                    "El archivo de imagen es obligatorio para crear un anuncio",
                    request
            );
        }

        var dateValidationResponse = validateAnnouncementDates(dto.getStartDate(), dto.getEndDate(), request, globalResponseHandler);
        if (dateValidationResponse != null) return dateValidationResponse;

        var optionalState = announcementStateRepository.findById(dto.getStateId());
        if (optionalState.isEmpty()) {
            return globalResponseHandler.badRequest(
                    "El estado de anuncio con ID " + dto.getStateId() + " no fue encontrado",
                    request
            );
        }

        String email = jwtService.extractUsername(jwtService.getTokenFromHeader(authHeader));
        var optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty() || optionalUser.get().getMunicipality() == null) {
            return globalResponseHandler.notFound(
                    "No se encontró la municipalidad del usuario autenticado",
                    request
            );
        }

        var municipality = optionalUser.get().getMunicipality();

        String imageUrl = tripo3DService.uploadToImgur(dto.getFile());

        var announcement = Announcement.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .state(optionalState.get())
                .imageUrl(imageUrl)
                .build();

        announcement.getMunicipalities().add(municipality);

        announcementRepository.save(announcement);

        notificationService.notifyAnnouncementCreationToMunicipalityUsers(
                NotificationTypeEnum.ANNOUNCEMENT_CREATED,
                null,
                municipality.getId(),
                email
        );

        return globalResponseHandler.handleResponse(
                "Anuncio creado correctamente para la municipalidad del usuario",
                announcement,
                HttpStatus.CREATED,
                request
        );
    }

    @PutMapping("/my-municipality/{id}")
    @PreAuthorize("hasRole('MUNICIPAL_ADMIN')")
    @Transactional
    public ResponseEntity<?> updateAnnouncementForMyMunicipality(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @ModelAttribute UpdateAnnouncementMultipartDTO dto,
            HttpServletRequest request
    ) throws IOException {
        logger.info("PUT /announcements/my-municipality/{} - Actualizando anuncio", id);

        var globalResponseHandler = new GlobalResponseHandler();


        var optionalUser = userRepository.findByEmail(
                jwtService.extractUsername(jwtService.getTokenFromHeader(authHeader))
        );

        if (optionalUser.isEmpty() || optionalUser.get().getMunicipality() == null) {
            return globalResponseHandler.notFound(
                    "No se encontró la municipalidad del usuario autenticado",
                    request
            );
        }

        var municipalityId = optionalUser.get().getMunicipality().getId();

        var optionalAnnouncement = announcementRepository.findByIdAndMunicipalities_Id(id, municipalityId);
        if (optionalAnnouncement.isEmpty()) {
            return globalResponseHandler.notFound(
                    "No se encontró el anuncio para esta municipalidad con ID " + id,
                    request
            );
        }

        var dateValidationResponse = validateAnnouncementDates(dto.getStartDate(), dto.getEndDate(), request, globalResponseHandler);
        if (dateValidationResponse != null) return dateValidationResponse;

        var announcement = optionalAnnouncement.get();

        if (dto.getStateId() != null && !announcementStateRepository.existsById(dto.getStateId())) {
            return globalResponseHandler.badRequest(
                    "El estado de anuncio con ID " + dto.getStateId() + " no fue encontrado",
                    request
            );
        }

        announcement.setTitle(dto.getTitle());
        announcement.setDescription(dto.getDescription());
        announcement.setStartDate(dto.getStartDate());
        announcement.setEndDate(dto.getEndDate());
        if (dto.getStateId() != null) {
            var optionalState = announcementStateRepository.findById(dto.getStateId());
            if (optionalState.isEmpty()) {
                return globalResponseHandler.badRequest(
                        "El estado de anuncio con ID " + dto.getStateId() + " no fue encontrado",
                        request
                );
            }
            announcement.setState(optionalState.get());
        }

        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            String imageUrl = tripo3DService.uploadToImgur(dto.getFile());
            announcement.setImageUrl(imageUrl);
        }

        announcementRepository.save(announcement);

        return globalResponseHandler.handleResponse(
                "Anuncio actualizado correctamente",
                announcement,
                HttpStatus.OK,
                request
        );
    }

    /**
     * Validates the start and end dates of an announcement.
     * @param startDate Date when the announcement starts.
     * @param endDate Date when the announcement ends.
     * @param request HTTP request for error handling.
     * @param handler GlobalResponseHandler for constructing responses.
     * @return ResponseEntity with validation results or null if valid.
     * @author dgutierrez
     */
    private ResponseEntity<?> validateAnnouncementDates(
            LocalDate startDate,
            LocalDate endDate,
            HttpServletRequest request,
            GlobalResponseHandler handler
    ) {
        if (startDate == null || endDate == null) {
            return handler.badRequest("Las fechas de inicio y fin del anuncio son obligatorias", request);
        }

        if (!startDate.isBefore(endDate)) {
            return handler.badRequest("La fecha de inicio debe ser anterior a la fecha de fin", request);
        }

        LocalDate today = LocalDate.now();
        LocalDate threeMonthsAgo = today.minusMonths(3);

        if (startDate.isBefore(threeMonthsAgo)) {
            return handler.badRequest("La fecha de inicio no puede ser anterior a hace 3 meses", request);
        }

        if (startDate.isAfter(today)) {
            return handler.badRequest("La fecha de inicio no puede ser posterior a hoy", request);
        }

        if (endDate.isAfter(startDate.plusYears(3))) {
            return handler.badRequest("La duración del anuncio no puede ser mayor a 3 años", request);
        }

        return null;
    }
}
