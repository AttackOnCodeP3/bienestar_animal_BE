package com.project.demo.logic.entity.auth;

import com.project.demo.logic.constants.security.PublicEndpointsConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration class that defines the authentication and authorization mechanisms for the application.
 *
 * Configures:
 * - Stateless session management
 * - JWT filter integration
 * - Public endpoints
 * - OAuth2 login support with a custom user service and success handler
 * @author dgutierrez
 * Updated by 
 * @author nav
 * - MODEL3D_ANIMAL added
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter,
                                 AuthenticationProvider authenticationProvider,
                                 CustomOAuth2UserService customOAuth2UserService,
                                 OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler,
                                 CustomAuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationProvider = authenticationProvider;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilita CSRF, ya que se trata de una API stateless protegida con JWT
                .csrf(AbstractHttpConfigurer::disable)

                // Configura un entry point personalizado para manejar errores de autenticación.
                // En lugar de redirigir al login (por defecto en OAuth2), responde con 401 Unauthorized.
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                )

                // Define reglas de autorización. Se permiten ciertas rutas públicas,
                // mientras que el resto requiere autenticación.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, PublicEndpointsConstants.AUTH).permitAll()
                        .requestMatchers(PublicEndpointsConstants.OAUTH2,
                                PublicEndpointsConstants.LOGIN,
                                PublicEndpointsConstants.INTERESTS,
                                PublicEndpointsConstants.MUNICIPALITIES,
                                PublicEndpointsConstants.CANTONS,
                                PublicEndpointsConstants.DISTRICTS,
                                PublicEndpointsConstants.NEIGHBORHOODS,
                                PublicEndpointsConstants.FORGOT_PASSWORD

                        ).permitAll()
                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                // Establece el proveedor de autenticación, normalmente para validar credenciales y cargar usuarios.
                .authenticationProvider(authenticationProvider)

                // Agrega el filtro de autenticación JWT antes del filtro de autenticación estándar de Spring Security.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Configura el login vía OAuth2 (Google), asignando el servicio que obtiene la información del usuario
                // y el handler que se ejecuta al autenticarse exitosamente.
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2LoginSuccessHandler)
                );

        return http.build();
    }
}