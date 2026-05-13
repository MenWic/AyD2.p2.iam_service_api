package ayd2.p2b.iam_service_api.unit.common.validation;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.common.validation.PasswordRules;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PasswordRulesTest {

    @Test
    void should_validate_required_password_length() {
        assertDoesNotThrow(() -> PasswordRules.validateRequiredPassword("Password123"));
    }

    @Test
    void should_reject_required_password_when_blank_or_invalid_length() {
        assertPasswordValidationError(() -> PasswordRules.validateRequiredPassword(null));
        assertPasswordValidationError(() -> PasswordRules.validateRequiredPassword("   "));
        assertPasswordValidationError(() -> PasswordRules.validateRequiredPassword("short"));
        assertPasswordValidationError(() -> PasswordRules.validateRequiredPassword("a".repeat(129)));
    }

    @Test
    void should_validate_optional_password_when_blank_or_null() {
        assertDoesNotThrow(() -> PasswordRules.validateOptionalPassword(null));
        assertDoesNotThrow(() -> PasswordRules.validateOptionalPassword(""));
        assertDoesNotThrow(() -> PasswordRules.validateOptionalPassword("   "));
    }

    @Test
    void should_reject_optional_password_when_nonblank_and_invalid_length() {
        assertPasswordValidationError(() -> PasswordRules.validateOptionalPassword("short"));
        assertPasswordValidationError(() -> PasswordRules.validateOptionalPassword("a".repeat(129)));
    }

    @Test
    void should_accept_optional_password_when_valid_length() {
        assertDoesNotThrow(() -> PasswordRules.validateOptionalPassword("Password123"));
    }

    private void assertPasswordValidationError(Runnable runnable) {
        ApiException ex = assertThrows(ApiException.class, runnable::run);
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("validation.failed", ex.getCode());
    }
}
