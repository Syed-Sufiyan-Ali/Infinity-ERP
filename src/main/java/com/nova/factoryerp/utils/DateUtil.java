package com.nova.factoryerp.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    public static final DateTimeFormatter DATE_FMT     = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    public static final DateTimeFormatter DB_DATE_FMT  = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String format(LocalDate d) {
        return d == null ? "" : d.format(DATE_FMT);
    }
    public static String format(LocalDateTime d) {
        return d == null ? "" : d.format(DATETIME_FMT);
    }
    public static LocalDate parseDate(String s) {
        try { return LocalDate.parse(s, DATE_FMT); }
        catch (Exception e) { return null; }
    }
    public static String today() {
        return LocalDate.now().format(DATE_FMT);
    }
    public static String generateSequence() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
