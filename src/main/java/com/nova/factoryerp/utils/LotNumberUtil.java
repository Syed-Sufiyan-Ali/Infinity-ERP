package com.nova.factoryerp.utils;

import java.time.Year;

public final class LotNumberUtil {

    private LotNumberUtil() {
        // Utility class
    }

    /**
     * Generates the standard internal lot-number format:
     *
     * LOT-{MATERIAL_CODE}-{YEAR}-{SEQUENCE}
     *
     * Example:
     * RM-005 + 2026 + 1
     * -> LOT-RM005-2026-001
     */
    public static String generate(
            String materialCode,
            int year,
            int sequence) {

        String normalizedCode = normalizeMaterialCode(materialCode);

        return String.format(
                "LOT-%s-%d-%03d",
                normalizedCode,
                year,
                sequence
        );
    }

    /**
     * Generates a lot number using the current year.
     */
    public static String generate(
            String materialCode,
            int sequence) {

        return generate(
                materialCode,
                Year.now().getValue(),
                sequence
        );
    }

    /**
     * Converts a material code such as RM-005 into RM005.
     */
    public static String normalizeMaterialCode(String materialCode) {

        if (materialCode == null || materialCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Material code cannot be empty."
            );
        }

        return materialCode
                .trim()
                .toUpperCase()
                .replaceAll("[^A-Z0-9]", "");
    }
}