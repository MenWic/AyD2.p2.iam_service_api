package ayd2.p2b.iam_service_api.common.validation;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public final class PasswordRules {

    private PasswordRules() {
    }

    public static void validateRequiredPassword(String password) {
        if (password == null || password.isBlank()) {
            throw validationError();
        }
        validateLength(password);
    }

    public static void validateOptionalPassword(String password) {
        if (password == null || password.isBlank()) {
            return;
        }
        validateLength(password);
    }

    private static void validateLength(String password) {
        if (password.length() < 8 || password.length() > 128) {
            throw validationError();
        }
    }

    private static ApiException validationError() {
        return new ApiException(HttpStatus.BAD_REQUEST, "validation.failed", "Password length must be between 8 and 128");
    }
}
