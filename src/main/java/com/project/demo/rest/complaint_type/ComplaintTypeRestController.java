package com.project.demo.rest.complaint_type;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.complaint_type.ComplaintType;
import com.project.demo.logic.entity.complaint_type.ComplaintTypeRepository;
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

@RestController
@RequestMapping("/complaint-types")
public class ComplaintTypeRestController {

    private static final Logger logger = LoggerFactory.getLogger(ComplaintTypeRestController.class);

    @Autowired
    private ComplaintTypeRepository complaintTypeRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        logger.info("Invocando getAll - obteniendo todos los tipos de denuncia. Página: {}, Tamaño: {}", page, size);
        var globalResponseHandler = new GlobalResponseHandler();

        Pageable pageable = PaginationUtils.buildPageable(page, size);
        Page<ComplaintType> complaintTypesPage = complaintTypeRepository.findAll(pageable);

        Meta meta = PaginationUtils.buildMeta(request, complaintTypesPage);

        return globalResponseHandler.handleResponse(
                "Tipos de denuncia obtenidos correctamente",
                complaintTypesPage.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getById(@PathVariable Long id, HttpServletRequest request) {

        logger.info("Invocando getById - obteniendo tipo de denuncia con ID: {}", id);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<ComplaintType> opt = complaintTypeRepository.findById(id);
        if (opt.isEmpty()) {
            logger.warn("Tipo de denuncia con ID {} no fue encontrado", id);
            return globalResponseHandler.notFound(
                    "El tipo de denuncia con ID " + id + " no fue encontrado",
                    request
            );
        }

        return globalResponseHandler.handleResponse(
                "Tipo de denuncia obtenido correctamente",
                opt.get(),
                HttpStatus.OK,
                request
        );
    }
}
