package com.project.demo.common;

/**
 * Utility class for Boolean operations.
 * Provides methods to handle Boolean values safely, avoiding null checks.
 * @author dgutierrez
 */
public class BooleanUtils {

    /**
     * Checks if the given Boolean value is true.
     * Returns true if the value is Boolean.TRUE, false otherwise (including null).
     *
     * @param value the Boolean value to check
     * @return true if value is Boolean.TRUE, false otherwise
     * @author dgutierrez
     */
    public static boolean isTrue(Boolean value) {
        return Boolean.TRUE.equals(value);
    }
}
