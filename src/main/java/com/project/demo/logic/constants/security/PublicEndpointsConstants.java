package com.project.demo.logic.constants.security;

/**
 * Defines public endpoints that are excluded from authentication requirements.
 * <p>
 * These constants are typically used in the security configuration to allow access to authentication-related routes.
 * <p>
 * AUTH: Endpoint for traditional authentication (e.g., /auth/login, /auth/register)
 * OAUTH2: Endpoint prefix used by Spring Security for handling OAuth2 authorization requests
 * LOGIN: Additional login-related paths handled internally by Spring Security
 * @author dgutierrez
 */
public class PublicEndpointsConstants {
    public static final String AUTH = "/auth/**";
    public static final String OAUTH2 = "/oauth2/**";
    public static final String LOGIN = "/login/**";
}
