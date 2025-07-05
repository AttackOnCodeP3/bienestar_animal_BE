package com.project.demo.rest.district;

import com.project.demo.logic.entity.district.District;
import com.project.demo.logic.entity.district.DistrictRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
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
@RequestMapping("/districts")
public class DistrictRestController {

    @Autowired
    private DistrictRepository districtRepository;

    @Autowired
    private NeighborhoodRepository neighborhoodRepository;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<District> districtPage = districtRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(districtPage.getTotalPages());
        meta.setTotalElements(districtPage.getTotalElements());
        meta.setPageNumber(districtPage.getNumber() + 1);
        meta.setPageSize(districtPage.getSize());

        return new GlobalResponseHandler().handleResponse("Districts retrieved successfully",
                districtPage.getContent(), HttpStatus.OK, meta);
    }

    @GetMapping("/{districtId}")
    public ResponseEntity<?> getById(@PathVariable Long districtId, HttpServletRequest request) {
        Optional<District> district = districtRepository.findById(districtId);
        if (district.isPresent()) {
            return new GlobalResponseHandler().handleResponse("District retrieved successfully",
                    district.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("District id " + districtId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @GetMapping("/{districtId}/neighborhoods")
    public ResponseEntity<?> getByDistrictId(@PathVariable Long districtId, HttpServletRequest request) {
        var neighborhoods = neighborhoodRepository.findByDistrictId(districtId);
        return new GlobalResponseHandler().handleResponse("Neighborhoods of district retrieved successfully",
                neighborhoods, HttpStatus.OK, request);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody District district, HttpServletRequest request) {
        District saved = districtRepository.save(district);
        return new GlobalResponseHandler().handleResponse("District created successfully",
                saved, HttpStatus.CREATED, request);
    }

    @PutMapping("/{districtId}")
    public ResponseEntity<?> update(@PathVariable Long districtId, @RequestBody District district, HttpServletRequest request) {
        Optional<District> found = districtRepository.findById(districtId);
        if (found.isPresent()) {
            district.setId(districtId);
            District updated = districtRepository.save(district);
            return new GlobalResponseHandler().handleResponse("District updated successfully",
                    updated, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("District id " + districtId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PatchMapping("/{districtId}")
    public ResponseEntity<?> patch(@PathVariable Long districtId, @RequestBody District district, HttpServletRequest request) {
        Optional<District> found = districtRepository.findById(districtId);
        if (found.isPresent()) {
            District existing = found.get();
            if (district.getName() != null) existing.setName(district.getName());
            if (district.getCanton() != null) existing.setCanton(district.getCanton());
            districtRepository.save(existing);
            return new GlobalResponseHandler().handleResponse("District updated successfully",
                    existing, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("District id " + districtId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @DeleteMapping("/{districtId}")
    public ResponseEntity<?> delete(@PathVariable Long districtId, HttpServletRequest request) {
        Optional<District> found = districtRepository.findById(districtId);
        if (found.isPresent()) {
            districtRepository.deleteById(districtId);
            return new GlobalResponseHandler().handleResponse("District deleted successfully",
                    found.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("District id " + districtId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }
}
