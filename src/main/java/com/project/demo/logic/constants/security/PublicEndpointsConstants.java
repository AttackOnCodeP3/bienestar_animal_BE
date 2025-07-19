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
    public static final String FORGOTPW = "/users/forgot-password**";
    public static final String CHANGEPW = "/users/password-reset/**";
    //TODO: DGUTIERREZ: Move these to require at least a hardcoded access token

    public static final String INTERESTS = "/interests/**";
    public static final String MUNICIPALITIES = "/municipalities/**";
    public static final String CANTONS = "/cantons/**";
    public static final String DISTRICTS = "/districts/**";
    public static final String NEIGHBORHOODS = "/neighborhoods/**";
}