package com.project.demo.rest.neighborhood;

import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.neighborhood.Neighborhood;
import com.project.demo.logic.entity.neighborhood.NeighborhoodRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/neighborhoods")
public class NeighborhoodRestController {

    @Autowired
    private NeighborhoodRepository neighborhoodRepository;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Neighborhood> neighborhoodPage = neighborhoodRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(neighborhoodPage.getTotalPages());
        meta.setTotalElements(neighborhoodPage.getTotalElements());
        meta.setPageNumber(neighborhoodPage.getNumber() + 1);
        meta.setPageSize(neighborhoodPage.getSize());

        return new GlobalResponseHandler().handleResponse("Neighborhoods retrieved successfully",
                neighborhoodPage.getContent(), HttpStatus.OK, meta);
    }

    @GetMapping("/{neighborhoodId}")
    public ResponseEntity<?> getById(@PathVariable Long neighborhoodId, HttpServletRequest request) {
        Optional<Neighborhood> neighborhood = neighborhoodRepository.findById(neighborhoodId);
        if (neighborhood.isPresent()) {
            return new GlobalResponseHandler().handleResponse("Neighborhood retrieved successfully",
                    neighborhood.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Neighborhood id " + neighborhoodId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @GetMapping("/districts/{districtId}/neighborhoods")
    public ResponseEntity<?> getByDistrictId(@PathVariable Long districtId, HttpServletRequest request) {
        var neighborhoods = neighborhoodRepository.findByDistrictId(districtId);
        return new GlobalResponseHandler().handleResponse("Neighborhoods of district retrieved successfully",
                neighborhoods, HttpStatus.OK, request);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Neighborhood neighborhood, HttpServletRequest request) {
        Neighborhood saved = neighborhoodRepository.save(neighborhood);
        return new GlobalResponseHandler().handleResponse("Neighborhood created successfully",
                saved, HttpStatus.CREATED, request);
    }

    @PutMapping("/{neighborhoodId}")
    public ResponseEntity<?> update(@PathVariable Long neighborhoodId, @RequestBody Neighborhood neighborhood, HttpServletRequest request) {
        Optional<Neighborhood> found = neighborhoodRepository.findById(neighborhoodId);
        if (found.isPresent()) {
            neighborhood.setId(neighborhoodId);
            Neighborhood updated = neighborhoodRepository.save(neighborhood);
            return new GlobalResponseHandler().handleResponse("Neighborhood updated successfully",
                    updated, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Neighborhood id " + neighborhoodId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PatchMapping("/{neighborhoodId}")
    public ResponseEntity<?> patch(@PathVariable Long neighborhoodId, @RequestBody Neighborhood neighborhood, HttpServletRequest request) {
        Optional<Neighborhood> found = neighborhoodRepository.findById(neighborhoodId);
        if (found.isPresent()) {
            Neighborhood existing = found.get();
            if (neighborhood.getName() != null) existing.setName(neighborhood.getName());
            if (neighborhood.getDistrict() != null) existing.setDistrict(neighborhood.getDistrict());
            neighborhoodRepository.save(existing);
            return new GlobalResponseHandler().handleResponse("Neighborhood updated successfully",
                    existing, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Neighborhood id " + neighborhoodId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @DeleteMapping("/{neighborhoodId}")
    public ResponseEntity<?> delete(@PathVariable Long neighborhoodId, HttpServletRequest request) {
        Optional<Neighborhood> found = neighborhoodRepository.findById(neighborhoodId);
        if (found.isPresent()) {
            neighborhoodRepository.deleteById(neighborhoodId);
            return new GlobalResponseHandler().handleResponse("Neighborhood deleted successfully",
                    found.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Neighborhood id " + neighborhoodId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }
}
