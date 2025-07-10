package com.project.demo.rest.rol;

import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.rol.Role;
import com.project.demo.logic.entity.rol.RoleRepository;
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
@RequestMapping("/roles")
public class RolRestController {

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Role> rolePage = roleRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(rolePage.getTotalPages());
        meta.setTotalElements(rolePage.getTotalElements());
        meta.setPageNumber(rolePage.getNumber() + 1);
        meta.setPageSize(rolePage.getSize());

        return new GlobalResponseHandler().handleResponse("Roles retrieved successfully",
                rolePage.getContent(), HttpStatus.OK, meta);
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<?> getById(@PathVariable Long roleId, HttpServletRequest request) {
        Optional<Role> role = roleRepository.findById(roleId);
        if (role.isPresent()) {
            return new GlobalResponseHandler().handleResponse("Role retrieved successfully",
                    role.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Role id " + roleId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Role role, HttpServletRequest request) {
        Role saved = roleRepository.save(role);
        return new GlobalResponseHandler().handleResponse("Role created successfully",
                saved, HttpStatus.CREATED, request);
    }

    @PutMapping("/{roleId}")
    public ResponseEntity<?> update(@PathVariable Long roleId, @RequestBody Role role, HttpServletRequest request) {
        Optional<Role> found = roleRepository.findById(roleId);
        if (found.isPresent()) {
            role.setId(roleId);
            Role updated = roleRepository.save(role);
            return new GlobalResponseHandler().handleResponse("Role updated successfully",
                    updated, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Role id " + roleId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PatchMapping("/{roleId}")
    public ResponseEntity<?> patch(@PathVariable Long roleId, @RequestBody Role role, HttpServletRequest request) {
        Optional<Role> found = roleRepository.findById(roleId);
        if (found.isPresent()) {
            Role existing = found.get();
            if (role.getName() != null) existing.setName(role.getName());
            if (role.getDescription() != null) existing.setDescription(role.getDescription());
            roleRepository.save(existing);
            return new GlobalResponseHandler().handleResponse("Role updated successfully",
                    existing, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Role id " + roleId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<?> delete(@PathVariable Long roleId, HttpServletRequest request) {
        Optional<Role> found = roleRepository.findById(roleId);
        if (found.isPresent()) {
            roleRepository.deleteById(roleId);
            return new GlobalResponseHandler().handleResponse("Role deleted successfully",
                    found.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Role id " + roleId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }
}
