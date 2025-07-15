package com.project.demo.rest.community_animal;

import com.project.demo.common.PaginationUtils;
import com.project.demo.logic.entity.auth.JwtService;
import com.project.demo.logic.entity.community_animal.CommunityAnimal;
import com.project.demo.logic.entity.community_animal.CommunityAnimalRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/community-animals")
public class CommunityAnimalRestController {

    private static final Logger logger = LoggerFactory.getLogger(CommunityAnimalRestController.class);

    @Autowired
    private CommunityAnimalRepository communityAnimalRepository;

    @Autowired
    private JwtService jwtService;

    /**
     * Returns the list of community animals registered by the currently authenticated user.
     *
     * @param authHeader Authorization header containing the JWT token
     * @param request    HTTP request (used for metadata)
     * @return ResponseEntity containing the list of animals
     */
    @GetMapping("/mine")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyAnimals(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        logger.info("Fetching community animals for authenticated user");

        String token = authHeader.replace("Bearer ", "");
        String email = jwtService.extractUsername(token);

        Pageable pageable = PaginationUtils.buildPageable(page, size);
        Page<CommunityAnimal> animalsPage = communityAnimalRepository.findByUser_Email(email, pageable);

        Meta meta = PaginationUtils.buildMeta(request, animalsPage);

        return new GlobalResponseHandler().handleResponse(
                "Community animals retrieved successfully",
                animalsPage.getContent(),
                HttpStatus.OK,
                meta
        );
    }
}

