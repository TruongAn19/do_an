package com.example.quanly.domain.utility;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    public static String format(LocalDateTime dateTime) {
        return dateTime.format(FORMATTER);
    }
}