package com.project.demo.rest.animal_type;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.animal_type.AnimalType;
import com.project.demo.logic.entity.animal_type.AnimalTypeRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
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
@RequestMapping("/animal-types")
public class AnimalTypeRestController {

    private static final Logger logger = LoggerFactory.getLogger(AnimalTypeRestController.class);

    @Autowired
    private AnimalTypeRepository animalTypeRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        logger.info("Fetching all animal types");

        Pageable pageable = PaginationUtils.buildPageable(page, size);
        Page<AnimalType> animalTypePage = animalTypeRepository.findAll(pageable);

        Meta meta = PaginationUtils.buildMeta(request, animalTypePage);

        return new GlobalResponseHandler().handleResponse(
                "Animal types retrieved successfully",
                animalTypePage.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getById(@PathVariable Long id, HttpServletRequest request) {
        logger.info("Fetching animal type with id: {}", id);
        Optional<AnimalType> opt = animalTypeRepository.findById(id);

        if (opt.isEmpty()) {
            logger.warn("Animal type with id {} not found", id);
            return new GlobalResponseHandler().handleResponse(
                    "Animal type id " + id + " not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }

        return new GlobalResponseHandler().handleResponse(
                "Animal type retrieved successfully",
                opt.get(),
                HttpStatus.OK,
                request
        );
    }
}
