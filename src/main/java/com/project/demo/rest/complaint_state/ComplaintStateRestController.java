package com.project.demo.rest.complaint_state;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.complaint_state.ComplaintState;
import com.project.demo.logic.entity.complaint_state.ComplaintStateRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.rest.complaint_state.dto.ComplaintStateDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/complaint-states")
public class ComplaintStateRestController {

    private static final Logger logger = LoggerFactory.getLogger(ComplaintStateRestController.class);

    @Autowired
    private ComplaintStateRepository complaintStateRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        logger.info("Invocando getAll - obteniendo todos los estados de denuncia. Página: {}, Tamaño: {}", page, size);
        var globalResponseHandler = new GlobalResponseHandler();

        Pageable pageable = PaginationUtils.buildPageable(page, size);
        Page<ComplaintState> pageResult = complaintStateRepository.findAll(pageable);

        var dtoList = pageResult.getContent()
                .stream()
                .map(ComplaintStateDTO::fromEntity)
                .collect(Collectors.toList());

        Meta meta = PaginationUtils.buildMeta(request, pageResult);

        return globalResponseHandler.handleResponse(
                "Estados de denuncia obtenidos correctamente",
                dtoList,
                HttpStatus.OK,
                meta
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getById(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        logger.info("Invocando getById - obteniendo estado de denuncia con ID: {}", id);
        var handler = new GlobalResponseHandler();

        Optional<ComplaintState> opt = complaintStateRepository.findById(id);
        if (opt.isEmpty()) {
            logger.warn("Estado de denuncia con ID {} no fue encontrado", id);
            return handler.notFound("El estado de denuncia con ID " + id + " no fue encontrado", request);
        }

        ComplaintStateDTO dto = ComplaintStateDTO.fromEntity(opt.get());

        return handler.handleResponse(
                "Estado de denuncia obtenido correctamente",
                dto,
                HttpStatus.OK,
                request
        );
    }
}
