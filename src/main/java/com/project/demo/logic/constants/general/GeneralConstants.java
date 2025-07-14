package com.project.demo.logic.constants.general;


/**
 * Seeder order constants for initializing data in the application.
 * <p>
 * These constants define the execution order for different seeders during
 * the application's data initialization phase. Lower values are executed first.
 * @author dgutierrez
 * 
 * modified by 
 * @author nav
 * State Generation Seeder added
 */
public class GeneralConstants {

    /**
     * URL to redirect the user after successful Google authentication.
     * <p>
     * This is typically the front-end route where the application handles the
     * authentication response and finalizes the login process.
     */
    public static final String SUCCESS_URL_AUTHENTICATION_GOOGLE = "http://localhost:4200/auth/social-callback";

    /**
     * Execution order for the Role seeder.
     * This should be one of the first to run since roles are typically needed for user creation.
     */
    public static final int ROLE_SEEDER_ORDER = 1;

    /**
     * Execution order for the Municipality Status seeder.
     * Initializes status types for municipalities.
     */
    public static final int MUNICIPALITY_STATUS_SEEDER_ORDER = 2;

    /**
     * Execution order for the Interest seeder.
     * Seeds predefined interests or categories relevant to the application.
     */
    public static final int INTEREST_SEEDER_ORDER = 3;

    /**
     * Execution order for the Locations seeder.
     * Loads geographical location data like states, cities, or regions.
     */
    public static final int LOCATIONS_SEEDER_ORDER = 4;

    /**
     * Execution order for the Municipality seeder.
     * Loads core municipality data used in the application.
     */
    public static final int MUNICIPALITY_SEEDER_ORDER = 5;

    /**
     * Execution order for the Admin seeder.
     * Creates default administrator users or system-level accounts.
     */
    public static final int ADMIN_SEEDER_ORDER = 6;
/**
 * Execution order for the StateGeneration seeder.
 * Creates default state generation statuses.
 */

public static final int STATE_GENERATION_SEEDER_ORDER = 7;

}