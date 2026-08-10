package com.nova.factoryerp.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class NumberUtil {
    private static final NumberFormat CURRENCY_FMT = NumberFormat.getNumberInstance(Locale.US);

    static {
        CURRENCY_FMT.setMinimumFractionDigits(2);
        CURRENCY_FMT.setMaximumFractionDigits(2);
    }

    public static String formatCurrency(BigDecimal value) {
        if (value == null) return "0.00";
        return "Rs " + CURRENCY_FMT.format(value);
    }

    public static String formatNumber(BigDecimal value) {
        if (value == null) return "0";
        return CURRENCY_FMT.format(value);
    }

    public static BigDecimal parseBigDecimal(String s) {
        try { return new BigDecimal(s.trim().replaceAll(",", "")); }
        catch (Exception e) { return BigDecimal.ZERO; }
    }

    public static int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return 0; }
    }

    public static String generateCode(String prefix, int sequence) {
        return prefix + String.format("%05d", sequence);
    }
}
