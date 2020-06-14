package com.github.fagnerlima.springspecificationtools.util;

import java.time.LocalDateTime;

/**
 * Utils for Date
 * @author Fagner Lima
 * @since 0.1.0
 */
public class DateUtils {

    /**
     * Combines this date-time with the time of midnight to create a {@code LocalDateTime} at the start of this date.
     *
     * @param dateTime the local date-time source.
     * @return the local date-time of midnight at the start of this date, not null.
     */
    public static LocalDateTime atStartOfDay(LocalDateTime dateTime) {
        return dateTime.toLocalDate().atStartOfDay();
    }

    /**
     * Combines this date-time with the time of 23:59:59 to create a {@code LocalDateTime} at the end of this date.
     *
     * @param dateTime the local date-time source.
     * @return the local date-time of midnight at the end of this date, not null.
     */
    public static LocalDateTime atEndOfDay(LocalDateTime dateTime) {
        return dateTime.toLocalDate().atTime(23, 59, 59);
    }

}
