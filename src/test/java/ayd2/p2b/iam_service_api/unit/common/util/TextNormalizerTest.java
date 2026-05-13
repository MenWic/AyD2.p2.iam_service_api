package ayd2.p2b.iam_service_api.unit.common.util;

import ayd2.p2b.iam_service_api.common.util.TextNormalizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TextNormalizerTest {

    @Test
    void should_trim_to_null_safely() {
        assertNull(TextNormalizer.trimToNull(null));
        assertNull(TextNormalizer.trimToNull("   "));
        assertEquals("value", TextNormalizer.trimToNull("  value  "));
    }

    @Test
    void should_trim_required_safely() {
        assertNull(TextNormalizer.trimRequired(null));
        assertEquals("", TextNormalizer.trimRequired("   "));
        assertEquals("value", TextNormalizer.trimRequired("  value  "));
    }

    @Test
    void should_normalize_email_as_trimmed_lowercase() {
        assertNull(TextNormalizer.normalizeEmail(null));
        assertEquals("user@domain.com", TextNormalizer.normalizeEmail("  User@Domain.COM  "));
    }
}
