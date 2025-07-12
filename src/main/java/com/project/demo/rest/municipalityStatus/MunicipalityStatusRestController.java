package com.project.demo.rest.municipalityStatus;

import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.municipality.MunicipalityStatus;
import com.project.demo.logic.entity.municipality.MunicipalityStatusRepository;
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
@RequestMapping("/municipality-statuses")
public class MunicipalityStatusRestController {

    @Autowired
    private MunicipalityStatusRepository municipalityStatusRepository;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<MunicipalityStatus> statusPage = municipalityStatusRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(statusPage.getTotalPages());
        meta.setTotalElements(statusPage.getTotalElements());
        meta.setPageNumber(statusPage.getNumber() + 1);
        meta.setPageSize(statusPage.getSize());

        return new GlobalResponseHandler().handleResponse("Municipality statuses retrieved successfully",
                statusPage.getContent(), HttpStatus.OK, meta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id, HttpServletRequest request) {
        Optional<MunicipalityStatus> found = municipalityStatusRepository.findById(id);
        if (found.isPresent()) {
            return new GlobalResponseHandler().handleResponse("Municipality status retrieved successfully",
                    found.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Municipality status id " + id + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody MunicipalityStatus status, HttpServletRequest request) {
        MunicipalityStatus saved = municipalityStatusRepository.save(status);
        return new GlobalResponseHandler().handleResponse("Municipality status created successfully",
                saved, HttpStatus.CREATED, request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody MunicipalityStatus status, HttpServletRequest request) {
        Optional<MunicipalityStatus> found = municipalityStatusRepository.findById(id);
        if (found.isPresent()) {
            status.setId(id);
            MunicipalityStatus updated = municipalityStatusRepository.save(status);
            return new GlobalResponseHandler().handleResponse("Municipality status updated successfully",
                    updated, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Municipality status id " + id + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patch(@PathVariable Long id, @RequestBody MunicipalityStatus status, HttpServletRequest request) {
        Optional<MunicipalityStatus> found = municipalityStatusRepository.findById(id);
        if (found.isPresent()) {
            MunicipalityStatus existing = found.get();
            if (status.getName() != null) existing.setName(status.getName());
            if (status.getDescription() != null) existing.setDescription(status.getDescription());
            MunicipalityStatus patched = municipalityStatusRepository.save(existing);
            return new GlobalResponseHandler().handleResponse("Municipality status updated successfully",
                    patched, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Municipality status id " + id + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Optional<MunicipalityStatus> found = municipalityStatusRepository.findById(id);
        if (found.isPresent()) {
            municipalityStatusRepository.deleteById(id);
            return new GlobalResponseHandler().handleResponse("Municipality status deleted successfully",
                    found.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Municipality status id " + id + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }
}
