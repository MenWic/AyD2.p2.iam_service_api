package ayd2.p2b.iam_service_api.unit.common.validation;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.common.validation.PersonalIdValidator;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersonalIdValidatorTest {

    @Test
    void should_accept_alphanumeric_values() {
        assertTrue(PersonalIdValidator.isAlphanumeric("A123B"));
        assertTrue(PersonalIdValidator.isAlphanumeric("abc123"));
    }

    @Test
    void should_reject_non_alphanumeric_values() {
        assertFalse(PersonalIdValidator.isAlphanumeric(null));
        assertFalse(PersonalIdValidator.isAlphanumeric(""));
        assertFalse(PersonalIdValidator.isAlphanumeric("A-123"));
        assertFalse(PersonalIdValidator.isAlphanumeric("A 123"));
    }

    @Test
    void should_throw_controlled_exception_when_value_is_invalid() {
        ApiException ex = assertThrows(ApiException.class, () -> PersonalIdValidator.validateAlphanumeric("A-123"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("validation.failed", ex.getCode());
    }
}
