package com.project.demo.rest.sanitary_control_response;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.sanitary_control_response.SanitaryControlResponse;
import com.project.demo.logic.entity.sanitary_control_response.SanitaryControlResponseRepository;
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

/**
 * REST controller for sanitary control response catalog.
 * Provides endpoints to retrieve all possible responses ("Sí", "No", "No sé").
 *
 * @author dgutierrez
 */
@RestController
@RequestMapping("/sanitary-control-responses")
public class SanitaryControlResponseRestController {

    private static final Logger logger = LoggerFactory.getLogger(SanitaryControlResponseRestController.class);

    @Autowired
    private SanitaryControlResponseRepository sanitaryControlResponseRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        logger.info("Fetching all sanitary control responses");

        Pageable pageable = PaginationUtils.buildPageable(page, size);
        Page<SanitaryControlResponse> responsePage = sanitaryControlResponseRepository.findAll(pageable);

        Meta meta = PaginationUtils.buildMeta(request, responsePage);

        return new GlobalResponseHandler().handleResponse(
                "Sanitary control responses retrieved successfully",
                responsePage.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getById(@PathVariable Long id, HttpServletRequest request) {
        logger.info("Fetching sanitary control response with id: {}", id);
        Optional<SanitaryControlResponse> opt = sanitaryControlResponseRepository.findById(id);
        if (opt.isEmpty()) {
            logger.warn("Sanitary control response with id {} not found", id);
            return new GlobalResponseHandler().handleResponse(
                    "Sanitary control response id " + id + " not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }
        return new GlobalResponseHandler().handleResponse(
                "Sanitary control response retrieved successfully",
                opt.get(),
                HttpStatus.OK,
                request
        );
    }
}
