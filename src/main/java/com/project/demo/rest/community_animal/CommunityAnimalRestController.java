package com.project.demo.rest.community_animal;

import com.project.demo.logic.entity.community_animal.CommunityAnimal;
import com.project.demo.logic.entity.community_animal.CommunityAnimalService;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.auth.JwtService;
import com.project.demo.rest.community_animal.dto.CreateAnimalRequestDTO;
import com.project.demo.common.PaginationUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/community-animals")
public class CommunityAnimalRestController {

    @Autowired private JwtService jwtService;
    @Autowired private CommunityAnimalService communityAnimalService;

    private static final Logger logger = LoggerFactory.getLogger(CommunityAnimalRestController.class);

    @PostMapping
    @PreAuthorize("hasRole('COMMUNITY_USER')")
    public ResponseEntity<?> createCommunityAnimal(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CreateAnimalRequestDTO dto,
            HttpServletRequest request) {

        var responseHandler = new GlobalResponseHandler();
        try {
            logger.info("Registrando nuevo animal comunitario...");
            String email = jwtService.extractUsername(jwtService.getTokenFromHeader(authHeader));
            CommunityAnimal animal = communityAnimalService.createCommunityAnimal(email, dto);
            return responseHandler.created("Animal comunitario registrado exitosamente.", animal, request);
        } catch (IllegalArgumentException ex) {
            return responseHandler.badRequest(ex.getMessage(), request);
        } catch (Exception ex) {
            logger.error("Error al registrar el animal comunitario", ex);
            return responseHandler.internalError("Error interno al registrar el animal", request);
        }
    }

    @GetMapping("/mine")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyAnimals(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        var responseHandler = new GlobalResponseHandler();
        try {
            String email = jwtService.extractUsername(jwtService.getTokenFromHeader(authHeader));
            Pageable pageable = PaginationUtils.buildPageable(page, size);
            var pageResult = communityAnimalService.getAnimalsByUser(email, pageable);
            Meta meta = PaginationUtils.buildMeta(request, pageResult);
            return responseHandler.handleResponse("Animales comunitarios obtenidos correctamente.", pageResult.getContent(), HttpStatus.OK, meta);
        } catch (Exception e) {
            logger.error("Error al obtener animales comunitarios del usuario", e);
            return responseHandler.internalError("Error al obtener animales comunitarios", request);
        }
    }
}