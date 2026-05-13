package ayd2.p2b.iam_service_api.common.validation;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import org.springframework.http.HttpStatus;

import java.util.regex.Pattern;

public final class PersonalIdValidator {

    private static final Pattern PERSONAL_ID_PATTERN = Pattern.compile("^[A-Za-z0-9]+$");

    private PersonalIdValidator() {
    }

    public static boolean isAlphanumeric(String value) {
        return value != null
                && !value.isBlank()
                && PERSONAL_ID_PATTERN.matcher(value).matches();
    }

    public static void validateAlphanumeric(String value) {
        if (!isAlphanumeric(value)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "validation.failed", "Invalid personalId format");
        }
    }
}
