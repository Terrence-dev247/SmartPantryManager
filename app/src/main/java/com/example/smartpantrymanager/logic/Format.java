package com.example.smartpantrymanager.logic;

import java.util.Calendar;
import java.util.Locale;

/** Small helpers for showing numbers and dates. */
public final class Format {

    private static final double MILLIS_PER_DAY = 24 * 60 * 60 * 1000.0;

    private Format() {
    }

    /** 2.0 -> "2", 0.5 -> "0.5", 1.25 -> "1.25". */
    public static String qty(double value) {
        if (Math.abs(value - Math.rint(value)) < 1e-9) {
            return String.valueOf((long) Math.rint(value));
        }
        String text = String.format(Locale.US, "%.2f", value);
        text = text.replaceAll("0+$", "");
        return text.endsWith(".") ? text.substring(0, text.length() - 1) : text;
    }

    /** "milk" -> "Milk". */
    public static String capitalise(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.substring(0, 1).toUpperCase(Locale.ROOT) + text.substring(1);
    }

    /** Whole days from "now" until the expiry date (negative if already expired). */
    public static int daysUntil(long expiryMillis, long nowMillis) {
        long expiryDay = startOfDay(expiryMillis);
        long today = startOfDay(nowMillis);
        return (int) Math.round((expiryDay - today) / MILLIS_PER_DAY);
    }

    private static long startOfDay(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
