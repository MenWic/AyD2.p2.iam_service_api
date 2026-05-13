package ayd2.p2b.iam_service_api.common.util;

import java.util.Locale;

public final class TextNormalizer {

    private TextNormalizer() {
    }

    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String trimRequired(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    public static String normalizeEmail(String email) {
        String trimmed = trimRequired(email);
        if (trimmed == null) {
            return null;
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }
}
