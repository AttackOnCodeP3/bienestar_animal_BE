package com.project.demo.logic.entity.animal.dto;

/**
 * Data Transfer Object representing the age of an animal in years, months, and days.
 * <p>
 * This record provides utility methods to format the age in a human-readable way,
 * check if the age is zero, or determine if the animal is considered a newborn.
 * It ensures that no negative values are allowed in its construction.
 * </p>
 *
 * @param years  number of full years
 * @param months number of additional months
 * @param days   number of additional days
 *
 * @author dgutierrez
 */
public record AgeDTO(int years, int months, int days) {

    /**
     * Compact constructor to validate the age values.
     * Throws an exception if any of the components are negative.
     */
    public AgeDTO {
        if (years < 0 || months < 0 || days < 0) {
            throw new IllegalArgumentException("Age values cannot be negative.");
        }
    }

    /**
     * Determines if the animal is considered a newborn (less than 30 days old).
     *
     * @return true if the age is less than 30 days, false otherwise
     */
    public boolean isNewborn() {
        return years == 0 && months == 0 && days < 30;
    }

    /**
     * Returns the age as a human-readable string.
     *
     * @return formatted string like "2 years, 3 months, 14 days"
     */
    public String toHumanReadable() {
        return String.format("%d years, %d months, %d days", years, months, days);
    }

    /**
     * Checks if the age is exactly zero.
     *
     * @return true if all components (years, months, days) are 0, false otherwise
     */
    public boolean isEmpty() {
        return years == 0 && months == 0 && days == 0;
    }
}
