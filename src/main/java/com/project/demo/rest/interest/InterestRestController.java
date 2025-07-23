package com.project.demo.rest.interest;

import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.interest.Interest;
import com.project.demo.logic.entity.interest.InterestRepository;
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
@RequestMapping("/interests")
public class InterestRestController {

    @Autowired
    private InterestRepository interestRepository;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Interest> interestPage = interestRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(interestPage.getTotalPages());
        meta.setTotalElements(interestPage.getTotalElements());
        meta.setPageNumber(interestPage.getNumber() + 1);
        meta.setPageSize(interestPage.getSize());

        return new GlobalResponseHandler().handleResponse("Interests retrieved successfully",
                interestPage.getContent(), HttpStatus.OK, meta);
    }

    @GetMapping("/{interestId}")
    public ResponseEntity<?> getById(@PathVariable Long interestId, HttpServletRequest request) {
        Optional<Interest> interest = interestRepository.findById(interestId);
        if (interest.isPresent()) {
            return new GlobalResponseHandler().handleResponse("Interest retrieved successfully",
                    interest.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Interest id " + interestId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Interest interest, HttpServletRequest request) {
        Interest saved = interestRepository.save(interest);
        return new GlobalResponseHandler().handleResponse("Interest created successfully",
                saved, HttpStatus.CREATED, request);
    }

    @PutMapping("/{interestId}")
    public ResponseEntity<?> update(@PathVariable Long interestId, @RequestBody Interest interest, HttpServletRequest request) {
        Optional<Interest> found = interestRepository.findById(interestId);
        if (found.isPresent()) {
            interest.setId(interestId);
            Interest updated = interestRepository.save(interest);
            return new GlobalResponseHandler().handleResponse("Interest updated successfully",
                    updated, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Interest id " + interestId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PatchMapping("/{interestId}")
    public ResponseEntity<?> patch(@PathVariable Long interestId, @RequestBody Interest interest, HttpServletRequest request) {
        Optional<Interest> found = interestRepository.findById(interestId);
        if (found.isPresent()) {
            Interest existing = found.get();
            if (interest.getName() != null) existing.setName(interest.getName());
            if (interest.getDescription() != null) existing.setDescription(interest.getDescription());
            interestRepository.save(existing);
            return new GlobalResponseHandler().handleResponse("Interest updated successfully",
                    existing, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Interest id " + interestId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @DeleteMapping("/{interestId}")
    public ResponseEntity<?> delete(@PathVariable Long interestId, HttpServletRequest request) {
        Optional<Interest> found = interestRepository.findById(interestId);
        if (found.isPresent()) {
            interestRepository.deleteById(interestId);
            return new GlobalResponseHandler().handleResponse("Interest deleted successfully",
                    found.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Interest id " + interestId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }
}
