package com.project.demo.rest.sex;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.sex.Sex;
import com.project.demo.logic.entity.sex.SexRepository;
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
@RequestMapping("/sex")
public class SexRestController {

    private static final Logger logger = LoggerFactory.getLogger(SexRestController.class);

    @Autowired
    private SexRepository sexRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        logger.info("Invocando getAll - obteniendo todos los valores de sexo. Página: {}, Tamaño: {}", page, size);
        var globalResponseHandler = new GlobalResponseHandler();

        Pageable pageable = PaginationUtils.buildPageable(page, size);
        Page<Sex> sexPage = sexRepository.findAll(pageable);

        Meta meta = PaginationUtils.buildMeta(request, sexPage);

        return globalResponseHandler.handleResponse(
                "Valores de sexo obtenidos correctamente",
                sexPage.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getById(@PathVariable Long id, HttpServletRequest request) {

        logger.info("Invocando getById - obteniendo valor de sexo con ID: {}", id);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<Sex> opt = sexRepository.findById(id);
        if (opt.isEmpty()) {
            logger.warn("Valor de sexo con ID {} no fue encontrado", id);
            return globalResponseHandler.notFound(
                    "El valor de sexo con ID " + id + " no fue encontrado",
                    request
            );
        }

        return globalResponseHandler.handleResponse(
                "Valor de sexo obtenido correctamente",
                opt.get(),
                HttpStatus.OK,
                request
        );
    }
}
