package ayd2.p2b.iam_service_api.feature.auth.application.exception;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public final class AuthExceptions {

    private AuthExceptions() {
    }

    public static ApiException invalidCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "auth.invalid_credentials", "Invalid email or password");
    }

    public static ApiException invalidRefreshToken() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "auth.token_invalid", "Refresh token is invalid");
    }

    public static ApiException blacklistedRefreshToken() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "auth.token_invalid", "Refresh token is invalidated");
    }

    public static ApiException validationFailed(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "validation.failed", message);
    }

    public static ApiException serviceTokenInvalid() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "auth.service_token_invalid", "Invalid service token");
    }
}
