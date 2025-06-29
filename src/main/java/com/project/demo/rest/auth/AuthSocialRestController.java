package com.project.demo.rest.auth;

import com.project.demo.logic.entity.user.LoginResponse;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.logic.entity.auth.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/social")
@RequiredArgsConstructor
public class AuthSocialRestController {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @GetMapping("/success")
    public ResponseEntity<LoginResponse> onSocialLoginSuccess(Authentication authentication) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado después del login social."));

        String token = jwtService.generateToken(user);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setAuthUser(user);
        response.setExpiresIn(3600);

        return ResponseEntity.ok(response);
    }
}
