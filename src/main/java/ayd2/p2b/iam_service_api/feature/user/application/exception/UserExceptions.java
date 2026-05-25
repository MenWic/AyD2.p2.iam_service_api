package ayd2.p2b.iam_service_api.feature.user.application.exception;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public final class UserExceptions {

    private UserExceptions() {
    }

    public static ApiException forbidden() {
        return new ApiException(HttpStatus.FORBIDDEN, "auth.forbidden", "Forbidden");
    }

    public static ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "resource.not_found", "User not found");
    }

    public static ApiException emailAlreadyRegistered() {
        return new ApiException(HttpStatus.CONFLICT, "resource.conflict", "Email already registered");
    }

    public static ApiException personalIdAlreadyRegistered() {
        return new ApiException(HttpStatus.CONFLICT, "resource.conflict", "Personal ID already registered");
    }

    public static ApiException multipleActiveUsersForPersonalId() {
        return new ApiException(HttpStatus.CONFLICT, "resource.conflict", "Multiple active users found for personalId");
    }

    public static ApiException invalidPersonalId() {
        return new ApiException(HttpStatus.BAD_REQUEST, "validation.failed", "Invalid personalId format");
    }

    public static ApiException validationFailed(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "validation.failed", message);
    }
}
