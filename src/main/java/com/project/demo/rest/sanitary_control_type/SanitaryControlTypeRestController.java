package com.project.demo.rest.sanitary_control_type;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.sanitary_control_type.SanitaryControlType;
import com.project.demo.logic.entity.sanitary_control_type.SanitaryControlTypeRepository;
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
@RequestMapping("/sanitary-control-types")
public class SanitaryControlTypeRestController {

    private static final Logger logger = LoggerFactory.getLogger(SanitaryControlTypeRestController.class);

    @Autowired
    private SanitaryControlTypeRepository sanitaryControlTypeRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        logger.info("Invocando getAll - obteniendo todos los tipos de control sanitario. Página: {}, Tamaño: {}", page, size);
        var globalResponseHandler = new GlobalResponseHandler();

        Pageable pageable = PaginationUtils.buildPageable(page, size);
        Page<SanitaryControlType> controlTypesPage = sanitaryControlTypeRepository.findAll(pageable);

        Meta meta = PaginationUtils.buildMeta(request, controlTypesPage);

        return globalResponseHandler.handleResponse(
                "Tipos de control sanitario obtenidos correctamente",
                controlTypesPage.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getById(@PathVariable Long id, HttpServletRequest request) {

        logger.info("Invocando getById - obteniendo tipo de control sanitario con ID: {}", id);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<SanitaryControlType> opt = sanitaryControlTypeRepository.findById(id);
        if (opt.isEmpty()) {
            logger.warn("Tipo de control sanitario con ID {} no fue encontrado", id);
            return globalResponseHandler.notFound(
                    "El tipo de control sanitario con ID " + id + " no fue encontrado",
                    request
            );
        }

        return globalResponseHandler.handleResponse(
                "Tipo de control sanitario obtenido correctamente",
                opt.get(),
                HttpStatus.OK,
                request
        );
    }
}
