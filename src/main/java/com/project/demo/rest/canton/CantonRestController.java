package com.project.demo.rest.canton;

import com.project.demo.logic.entity.canton.Canton;
import com.project.demo.logic.entity.canton.CantonRepository;
import com.project.demo.logic.entity.district.DistrictRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.logging.Logger;

@RestController
@RequestMapping("/cantons")
public class CantonRestController {

    @Autowired
    private CantonRepository cantonRepository;

    @Autowired
    private DistrictRepository districtRepository;

    private final Logger logger = Logger.getLogger(CantonRestController.class.getName());

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Canton> cantonPage = cantonRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(cantonPage.getTotalPages());
        meta.setTotalElements(cantonPage.getTotalElements());
        meta.setPageNumber(cantonPage.getNumber() + 1);
        meta.setPageSize(cantonPage.getSize());

        return new GlobalResponseHandler().handleResponse("Cantons retrieved successfully",
                cantonPage.getContent(), HttpStatus.OK, meta);
    }

    @GetMapping("/{cantonId}")
    public ResponseEntity<?> getById(@PathVariable Long cantonId, HttpServletRequest request) {
        Optional<Canton> canton = cantonRepository.findById(cantonId);
        if (canton.isPresent()) {
            return new GlobalResponseHandler().handleResponse("Canton retrieved successfully",
                    canton.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Canton id " + cantonId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @GetMapping("/{cantonId}/districts")
    public ResponseEntity<?> getByCantonId(@PathVariable Long cantonId, HttpServletRequest request) {
        logger.info("Getting districts for canton with ID: " + cantonId);
        var districts = districtRepository.findByCantonId(cantonId);
        return new GlobalResponseHandler().handleResponse("Districts of canton retrieved successfully",
                districts, HttpStatus.OK, request);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Canton canton, HttpServletRequest request) {
        Canton saved = cantonRepository.save(canton);
        return new GlobalResponseHandler().handleResponse("Canton created successfully",
                saved, HttpStatus.CREATED, request);
    }

    @PutMapping("/{cantonId}")
    public ResponseEntity<?> update(@PathVariable Long cantonId, @RequestBody Canton canton, HttpServletRequest request) {
        Optional<Canton> found = cantonRepository.findById(cantonId);
        if (found.isPresent()) {
            canton.setId(cantonId);
            Canton updated = cantonRepository.save(canton);
            return new GlobalResponseHandler().handleResponse("Canton updated successfully",
                    updated, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Canton id " + cantonId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PatchMapping("/{cantonId}")
    public ResponseEntity<?> patch(@PathVariable Long cantonId, @RequestBody Canton canton, HttpServletRequest request) {
        Optional<Canton> found = cantonRepository.findById(cantonId);
        if (found.isPresent()) {
            Canton existing = found.get();
            if (canton.getName() != null) existing.setName(canton.getName());
            cantonRepository.save(existing);
            return new GlobalResponseHandler().handleResponse("Canton updated successfully",
                    existing, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Canton id " + cantonId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @DeleteMapping("/{cantonId}")
    public ResponseEntity<?> delete(@PathVariable Long cantonId, HttpServletRequest request) {
        Optional<Canton> found = cantonRepository.findById(cantonId);
        if (found.isPresent()) {
            cantonRepository.deleteById(cantonId);
            return new GlobalResponseHandler().handleResponse("Canton deleted successfully",
                    found.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Canton id " + cantonId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }
}
