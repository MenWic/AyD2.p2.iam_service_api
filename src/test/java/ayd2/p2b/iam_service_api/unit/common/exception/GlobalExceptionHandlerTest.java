package ayd2.p2b.iam_service_api.unit.common.exception;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.common.exception.GlobalExceptionHandler;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void should_preserve_api_exception_status_code_and_detail() {
        ApiException exception = new ApiException(HttpStatus.FORBIDDEN, "auth.forbidden", "Forbidden");

        ProblemDetail detail = handler.handleApiException(exception);

        assertEquals(HttpStatus.FORBIDDEN.value(), detail.getStatus());
        assertEquals("Forbidden", detail.getDetail());
        assertEquals("auth.forbidden", detail.getProperties().get("code"));
    }

    @Test
    void should_return_validation_failed_for_constraint_violation_exception() {
        ConstraintViolationException exception = new ConstraintViolationException("Invalid request", Set.of());

        ProblemDetail detail = handler.handleConstraintViolation(exception);

        assertEquals(HttpStatus.BAD_REQUEST.value(), detail.getStatus());
        assertEquals("Validation failed", detail.getDetail());
        assertEquals("validation.failed", detail.getProperties().get("code"));
    }

    @Test
    void should_return_internal_error_for_unexpected_exception() {
        ProblemDetail detail = handler.handleUnexpected(new RuntimeException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), detail.getStatus());
        assertEquals("Unexpected error", detail.getDetail());
        assertEquals("system.internal_error", detail.getProperties().get("code"));
    }
}
