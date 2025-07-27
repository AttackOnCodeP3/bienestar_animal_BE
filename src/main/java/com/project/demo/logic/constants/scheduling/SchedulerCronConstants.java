package com.project.demo.logic.constants.scheduling;

/**
 * Common cron expressions used for scheduled tasks.
 * <p>
 * Each constant represents a cron expression for a specific frequency.
 * @author dgutierrez
 */
public class SchedulerCronConstants {

    public static final String EVERY_SECOND = "*/1 * * * * *";
    public static final String EVERY_30_SECONDS = "*/30 * * * * *";
    public static final String EVERY_MINUTE = "0 * * * * *";
    public static final String EVERY_5_MINUTES = "0 */5 * * * *";
    public static final String EVERY_HOUR = "0 0 * * * *";
    public static final String EVERY_MIDNIGHT = "0 0 0 * * *";

    public static final String ZONE_AMERICA_COSTA_RICA = "America/Costa_Rica";
    public static final int DAYS_BETWEEN_NOTIFICATIONS = 7;

    private SchedulerCronConstants() {
    }
}
