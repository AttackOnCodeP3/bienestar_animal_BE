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

        logger.info("Fetching all sanitary control records");

        Pageable pageable = PaginationUtils.buildPageable(page, size);
        Page<SanitaryControl> sanitaryControlPage = sanitaryControlRepository.findAll(pageable);

        Meta meta = PaginationUtils.buildMeta(request, sanitaryControlPage);

        return new GlobalResponseHandler().handleResponse(
                "Sanitary control records retrieved successfully",
                sanitaryControlPage.getContent(),
                HttpStatus.OK,
                meta
        );
    }


    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getById(@PathVariable Long id, HttpServletRequest request) {
        logger.info("Fetching sanitary control record with id: {}", id);
        Optional<SanitaryControl> opt = sanitaryControlRepository.findById(id);
        if (opt.isEmpty()) {
            logger.warn("Sanitary control record with id {} not found", id);
            return new GlobalResponseHandler().handleResponse(
                    "Sanitary control id " + id + " not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }
        return new GlobalResponseHandler().handleResponse(
                "Sanitary control record retrieved successfully",
                opt.get(),
                HttpStatus.OK,
                request
        );
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> create(@RequestBody SanitaryControl sanitaryControl, HttpServletRequest request) {
        logger.info("Creating sanitary control record for type: {}", sanitaryControl.getSanitaryControlType().getName());
        sanitaryControlRepository.save(sanitaryControl);
        return new GlobalResponseHandler().handleResponse(
                "Sanitary control record created successfully",
                sanitaryControl,
                HttpStatus.CREATED,
                request
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SanitaryControl sanitaryControl, HttpServletRequest request) {
        logger.info("Updating sanitary control record with id: {}", id);
        Optional<SanitaryControl> opt = sanitaryControlRepository.findById(id);
        if (opt.isEmpty()) {
            logger.warn("Sanitary control record with id {} not found", id);
            return new GlobalResponseHandler().handleResponse(
                    "Sanitary control id " + id + " not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }
        SanitaryControl current = opt.get();
        current.setLastApplicationDate(sanitaryControl.getLastApplicationDate());
        current.setProductUsed(sanitaryControl.getProductUsed());
        current.setSanitaryControlType(sanitaryControl.getSanitaryControlType());

        sanitaryControlRepository.save(current);
        return new GlobalResponseHandler().handleResponse(
                "Sanitary control record updated successfully",
                current,
                HttpStatus.OK,
                request
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {
        logger.info("Deleting sanitary control record with id: {}", id);
        Optional<SanitaryControl> opt = sanitaryControlRepository.findById(id);
        if (opt.isPresent()) {
            sanitaryControlRepository.deleteById(id);
            logger.info("Sanitary control record with id {} deleted", id);
            return new GlobalResponseHandler().handleResponse(
                    "Sanitary control record deleted successfully",
                    opt.get(),
                    HttpStatus.OK,
                    request
            );
        } else {
            logger.warn("Sanitary control record with id {} not found", id);
            return new GlobalResponseHandler().handleResponse(
                    "Sanitary control id " + id + " not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }
    }
}
