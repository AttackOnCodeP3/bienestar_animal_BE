package com.project.demo.rest.municipality;

import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.municipality.Municipality;
import com.project.demo.logic.entity.municipality.MunicipalityRepository;
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
@RequestMapping("/municipalities")
public class MunicipalityRestController {

    @Autowired
    private MunicipalityRepository municipalityRepository;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Municipality> municipalityPage = municipalityRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(municipalityPage.getTotalPages());
        meta.setTotalElements(municipalityPage.getTotalElements());
        meta.setPageNumber(municipalityPage.getNumber() + 1);
        meta.setPageSize(municipalityPage.getSize());

        return new GlobalResponseHandler().handleResponse("Municipalities retrieved successfully",
                municipalityPage.getContent(), HttpStatus.OK, meta);
    }

    @GetMapping("/{municipalityId}")
    public ResponseEntity<?> getById(@PathVariable Long municipalityId, HttpServletRequest request) {
        Optional<Municipality> municipality = municipalityRepository.findById(municipalityId);
        if (municipality.isPresent()) {
            return new GlobalResponseHandler().handleResponse("Municipality retrieved successfully",
                    municipality.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Municipality id " + municipalityId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Municipality municipality, HttpServletRequest request) {
        Municipality saved = municipalityRepository.save(municipality);
        return new GlobalResponseHandler().handleResponse("Municipality created successfully",
                saved, HttpStatus.CREATED, request);
    }

    @PutMapping("/{municipalityId}")
    public ResponseEntity<?> update(@PathVariable Long municipalityId, @RequestBody Municipality municipality, HttpServletRequest request) {
        Optional<Municipality> found = municipalityRepository.findById(municipalityId);
        if (found.isPresent()) {
            municipality.setId(municipalityId);
            Municipality updated = municipalityRepository.save(municipality);
            return new GlobalResponseHandler().handleResponse("Municipality updated successfully",
                    updated, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Municipality id " + municipalityId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PatchMapping("/{municipalityId}")
    public ResponseEntity<?> patch(@PathVariable Long municipalityId, @RequestBody Municipality municipality, HttpServletRequest request) {
        Optional<Municipality> found = municipalityRepository.findById(municipalityId);
        if (found.isPresent()) {
            Municipality existing = found.get();
            if (municipality.getName() != null) existing.setName(municipality.getName());
            if (municipality.getAddress() != null) existing.setAddress(municipality.getAddress());
            if (municipality.getPhone() != null) existing.setPhone(municipality.getPhone());
            if (municipality.getEmail() != null) existing.setEmail(municipality.getEmail());
            if (municipality.getCanton() != null) existing.setCanton(municipality.getCanton());
            municipalityRepository.save(existing);
            return new GlobalResponseHandler().handleResponse("Municipality updated successfully",
                    existing, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Municipality id " + municipalityId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @DeleteMapping("/{municipalityId}")
    public ResponseEntity<?> delete(@PathVariable Long municipalityId, HttpServletRequest request) {
        Optional<Municipality> found = municipalityRepository.findById(municipalityId);
        if (found.isPresent()) {
            municipalityRepository.deleteById(municipalityId);
            return new GlobalResponseHandler().handleResponse("Municipality deleted successfully",
                    found.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Municipality id " + municipalityId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }
}
