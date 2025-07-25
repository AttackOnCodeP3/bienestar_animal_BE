package com.project.demo.logic.constants.general;


/**
 * Seeder order constants for initializing data in the application.
 * <p>
 * These constants define the execution order for different seeders during
 * the application's data initialization phase. Lower values are executed first.
 *
 * @author dgutierrez
 * <p>
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
     * Execution order for the Sanitary Control Response Seeder.
     * Seeds predefined responses or actions related to sanitary controls.
     * This is important for managing compliance and reporting in the application.
     */
    public static final int SANITARY_CONTROL_RESPONSE_SEEDER_ORDER = 2;

    /**
     * Execution order for the Municipality Status seeder.
     * Initializes status types for municipalities.
     */
    public static final int MUNICIPALITY_STATUS_SEEDER_ORDER = 3;

    /**
     * Execution order for the Interest seeder.
     * Seeds predefined interests or categories relevant to the application.
     */
    public static final int INTEREST_SEEDER_ORDER = 4;

    /**
     * Execution order for the Locations seeder.
     * Loads geographical location data like states, cities, or regions.
     */
    public static final int LOCATIONS_SEEDER_ORDER = 5;

    /**
     * Execution order for the Sanitary Control seeder.
     * Seeds data related to sanitary controls, such as inspections or compliance checks.
     */
    public static final int SANITARY_CONTROL_TYPE_SEEDER_ORDER = 6;

    /**
     * Execution order for the Seeder seeder.
     * Seeds data related to the Seeder entity, which may include information about users
     * or entities that perform seeding actions.
     */
    public static final int SEX_SEEDER_ORDER = 7;

    /**
     * Execution order for the Animal Type seeder.
     * Seeds data related to different types of animals, such as species or breeds.
     */
    public static final int ANIMAL_TYPE_SEEDER_ORDER = 8;

    /**
     * Execution order for the Species Seeder.
     * Seeds data related to specific species of animals, which may include
     * scientific names, common names, and other relevant information.
     */
    public static final int SPECIES_SEEDER_ORDER = 9;

    /**
     * Execution order for the Vaccine Seeder.
     * Seeds data related to vaccines, including types, dosages,
     * and administration protocols.
     * This is important for managing animal health
     * and vaccination records.
     */
    public static final int VACCINE_SEEDER_ORDER = 10;

    /**
     * Execution order for the Race Seeder.
     * Seeds data related to different breeds or races
     * of animals, which may include characteristics,
     * classifications, and other relevant information.
     */
    public static final int RACE_SEEDER_ORDER = 11;

    /**
     * Execution order for the Municipality seeder.
     * Loads core municipality data used in the application.
     */
    public static final int MUNICIPALITY_SEEDER_ORDER = 12;

    /**
     * Execution order for the Admin seeder.
     * Creates default administrator users or system-level accounts.
     */
    public static final int ADMIN_SEEDER_ORDER = 13;

    /**
     * Regular expression for validating secure passwords.
     * <p>
     * This regex requires at least 8 characters, one uppercase letter,
     * one lowercase letter, one digit, and one special character.
     */
    public static final String SECURE_PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

    /**
     * Message to display when a password does not meet security requirements.
     * <p>
     * This message informs the user about the specific requirements for a secure password.
     */
    public static final String SECURE_PASSWORD_MESSAGE = "La contraseña debe tener al menos 8 caracteres, una letra mayúscula, una letra minúscula, un dígito y un carácter especial.";
    /**
     * Execution order for the StateGeneration seeder.
     * Creates default state generation statuses.
     */
    public static final int STATE_GENERATION_SEEDER_ORDER = 14;

    /**
     * Execution order for the Community Animal Seeder.
     * Seeds data related to community animals
     */
    public static final int COMMUNITY_ANIMAL_SEEDER_ORDER = 15;

}