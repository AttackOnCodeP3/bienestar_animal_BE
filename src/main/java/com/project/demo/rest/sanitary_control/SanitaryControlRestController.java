package com.project.demo.rest.sanitary_control;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.sanitary_control.SanitaryControl;
import com.project.demo.logic.entity.sanitary_control.SanitaryControlRepository;
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
@RequestMapping("/sanitary-controls")
public class SanitaryControlRestController {

    private static final Logger logger = LoggerFactory.getLogger(SanitaryControlRestController.class);

    @Autowired
    private SanitaryControlRepository sanitaryControlRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        logger.info("Invocando getAll - obteniendo todos los registros de control sanitario. Página: {}, Tamaño: {}", page, size);
        var globalResponseHandler = new GlobalResponseHandler();

        Pageable pageable = PaginationUtils.buildPageable(page, size);
        Page<SanitaryControl> sanitaryControlPage = sanitaryControlRepository.findAll(pageable);

        Meta meta = PaginationUtils.buildMeta(request, sanitaryControlPage);

        return globalResponseHandler.handleResponse(
                "Registros de control sanitario obtenidos correctamente",
                sanitaryControlPage.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getById(@PathVariable Long id, HttpServletRequest request) {
        logger.info("Invocando getById - obteniendo registro de control sanitario con ID: {}", id);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<SanitaryControl> opt = sanitaryControlRepository.findById(id);
        if (opt.isEmpty()) {
            logger.warn("Registro de control sanitario con ID {} no fue encontrado", id);
            return globalResponseHandler.notFound(
                    "El registro de control sanitario con ID " + id + " no fue encontrado",
                    request
            );
        }

        return globalResponseHandler.handleResponse(
                "Registro de control sanitario obtenido correctamente",
                opt.get(),
                HttpStatus.OK,
                request
        );
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> create(@RequestBody SanitaryControl sanitaryControl, HttpServletRequest request) {
        logger.info("Invocando create - creando nuevo registro de control sanitario para tipo: {}", sanitaryControl.getSanitaryControlType().getName());
        var globalResponseHandler = new GlobalResponseHandler();

        sanitaryControlRepository.save(sanitaryControl);
        return globalResponseHandler.created(
                "Registro de control sanitario creado correctamente",
                sanitaryControl,
                request
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SanitaryControl sanitaryControl, HttpServletRequest request) {
        logger.info("Invocando update - actualizando registro de control sanitario con ID: {}", id);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<SanitaryControl> opt = sanitaryControlRepository.findById(id);
        if (opt.isEmpty()) {
            logger.warn("Registro de control sanitario con ID {} no fue encontrado", id);
            return globalResponseHandler.notFound(
                    "El registro de control sanitario con ID " + id + " no fue encontrado",
                    request
            );
        }

        SanitaryControl current = opt.get();
        current.setLastApplicationDate(sanitaryControl.getLastApplicationDate());
        current.setProductUsed(sanitaryControl.getProductUsed());
        current.setSanitaryControlType(sanitaryControl.getSanitaryControlType());

        sanitaryControlRepository.save(current);
        return globalResponseHandler.success(
                "Registro de control sanitario actualizado correctamente",
                current,
                request
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {
        logger.info("Invocando delete - eliminando registro de control sanitario con ID: {}", id);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<SanitaryControl> opt = sanitaryControlRepository.findById(id);
        if (opt.isPresent()) {
            sanitaryControlRepository.deleteById(id);
            logger.info("Registro de control sanitario con ID {} eliminado correctamente", id);
            return globalResponseHandler.success(
                    "Registro de control sanitario eliminado correctamente",
                    opt.get(),
                    request
            );
        } else {
            logger.warn("Registro de control sanitario con ID {} no fue encontrado", id);
            return globalResponseHandler.notFound(
                    "El registro de control sanitario con ID " + id + " no fue encontrado",
                    request
            );
        }
    }
}
