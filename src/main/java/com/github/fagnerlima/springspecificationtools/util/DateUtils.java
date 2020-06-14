package com.github.fagnerlima.springspecificationtools.util;

import java.time.LocalDateTime;

public class DateUtils {

    public static LocalDateTime atStartOfDay(LocalDateTime dateTime) {
        return dateTime.toLocalDate().atStartOfDay();
    }

    public static LocalDateTime atEndOfDay(LocalDateTime dateTime) {
        return dateTime.toLocalDate().atTime(23, 59, 59);
    }

}
