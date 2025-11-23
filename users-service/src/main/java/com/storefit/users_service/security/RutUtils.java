package com.storefit.users_service.security;

import java.util.Locale;

public final class RutUtils {
    private static final String DOTTED_REGEX = "^\\d{1,3}\\.\\d{3}\\.\\d{3}-[0-9Kk]$";

    private RutUtils() {}

    public static boolean isDottedFormat(String rut) {
        return rut != null && rut.matches(DOTTED_REGEX);
    }

    public static String removeDots(String rut) {
        if (rut == null) return null;
        return rut.replace(".", "");
    }

    // Returns canonical dotted form if input contains hyphen and numbers; does not compute DV
    public static String toCanonicalDotted(String rut) {
        if (rut == null || rut.isBlank()) return rut;
        String cleaned = rut.replace(".", "").replace(" ", "");
        int hyphen = cleaned.lastIndexOf('-');
        if (hyphen <= 0 || hyphen >= cleaned.length()-1) {
            return rut; // cannot reformat without hyphen; return as is
        }
        String body = cleaned.substring(0, hyphen);
        String dv = cleaned.substring(hyphen+1).toUpperCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder();
        int len = body.length();
        int firstGroup = len % 3;
        if (firstGroup == 0) firstGroup = 3;
        sb.append(body, 0, firstGroup);
        int i = firstGroup;
        while (i < len) {
            sb.append('.')
              .append(body, i, i + 3);
            i += 3;
        }
        sb.append('-').append(dv);
        return sb.toString();
    }

    public static String requireDottedOrBadRequest(String rut) {
        if (!isDottedFormat(rut)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "RUT debe venir con puntos y guion (ej: 12.345.678-9)");
        }
        return rut;
    }
}

