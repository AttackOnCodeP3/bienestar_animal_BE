package com.project.demo.rest.rol;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.rol.Role;
import com.project.demo.logic.entity.rol.RoleEnum;
import com.project.demo.logic.entity.rol.RoleRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(RolRestController.class);

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        logger.info("Invocando getAll - obteniendo todos los roles. Página: {}, Tamaño: {}", page, size);
        var globalResponseHandler = new GlobalResponseHandler();

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Role> rolePage = roleRepository.findAllByNameNot(RoleEnum.SUPER_ADMIN, pageable);

        Meta meta = PaginationUtils.buildMeta(request, rolePage);

        return globalResponseHandler.handleResponse(
                "Roles obtenidos correctamente",
                rolePage.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<?> getById(@PathVariable Long roleId, HttpServletRequest request) {
        logger.info("Invocando getById - buscando rol con ID: {}", roleId);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<Role> role = roleRepository.findById(roleId);
        if (role.isPresent()) {
            return globalResponseHandler.handleResponse(
                    "Rol obtenido correctamente",
                    role.get(),
                    HttpStatus.OK,
                    request
            );
        } else {
            logger.warn("Rol con ID {} no encontrado", roleId);
            return globalResponseHandler.notFound(
                    "El rol con ID " + roleId + " no fue encontrado",
                    request
            );
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Role role, HttpServletRequest request) {
        logger.info("Invocando create - creando nuevo rol: {}", role.getName());
        var globalResponseHandler = new GlobalResponseHandler();

        Role saved = roleRepository.save(role);
        return globalResponseHandler.created(
                "Rol creado correctamente",
                saved,
                request
        );
    }

    @PutMapping("/{roleId}")
    public ResponseEntity<?> update(@PathVariable Long roleId, @RequestBody Role role, HttpServletRequest request) {
        logger.info("Invocando update - actualizando rol con ID: {}", roleId);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<Role> found = roleRepository.findById(roleId);
        if (found.isPresent()) {
            role.setId(roleId);
            Role updated = roleRepository.save(role);
            return globalResponseHandler.handleResponse(
                    "Rol actualizado correctamente",
                    updated,
                    HttpStatus.OK,
                    request
            );
        } else {
            logger.warn("Rol con ID {} no encontrado", roleId);
            return globalResponseHandler.notFound(
                    "El rol con ID " + roleId + " no fue encontrado",
                    request
            );
        }
    }

    @PatchMapping("/{roleId}")
    public ResponseEntity<?> patch(@PathVariable Long roleId, @RequestBody Role role, HttpServletRequest request) {
        logger.info("Invocando patch - actualizando parcialmente el rol con ID: {}", roleId);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<Role> found = roleRepository.findById(roleId);
        if (found.isPresent()) {
            Role existing = found.get();
            if (role.getName() != null) existing.setName(role.getName());
            if (role.getDescription() != null) existing.setDescription(role.getDescription());

            roleRepository.save(existing);

            return globalResponseHandler.handleResponse(
                    "Rol actualizado correctamente",
                    existing,
                    HttpStatus.OK,
                    request
            );
        } else {
            logger.warn("Rol con ID {} no encontrado", roleId);
            return globalResponseHandler.notFound(
                    "El rol con ID " + roleId + " no fue encontrado",
                    request
            );
        }
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<?> delete(@PathVariable Long roleId, HttpServletRequest request) {
        logger.info("Invocando delete - eliminando rol con ID: {}", roleId);
        var globalResponseHandler = new GlobalResponseHandler();

        Optional<Role> found = roleRepository.findById(roleId);
        if (found.isPresent()) {
            roleRepository.deleteById(roleId);
            logger.info("Rol con ID {} eliminado", roleId);
            return globalResponseHandler.handleResponse(
                    "Rol eliminado correctamente",
                    found.get(),
                    HttpStatus.OK,
                    request
            );
        } else {
            logger.warn("Rol con ID {} no encontrado", roleId);
            return globalResponseHandler.notFound(
                    "El rol con ID " + roleId + " no fue encontrado",
                    request
            );
        }
    }
}