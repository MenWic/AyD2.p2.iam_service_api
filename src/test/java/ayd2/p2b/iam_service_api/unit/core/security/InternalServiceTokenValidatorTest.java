package ayd2.p2b.iam_service_api.unit.core.security;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.core.security.InternalServiceProperties;
import ayd2.p2b.iam_service_api.core.security.InternalServiceTokenValidator;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InternalServiceTokenValidatorTest {

    @Test
    void should_throw_unauthorized_when_configured_service_token_is_missing() {
        InternalServiceProperties properties = new InternalServiceProperties();
        properties.setServiceToken("   ");
        InternalServiceTokenValidator validator = new InternalServiceTokenValidator(properties);

        ApiException ex = assertThrows(ApiException.class, () -> validator.validate("conference-token"));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("auth.service_token_invalid", ex.getCode());
    }

    @Test
    void should_throw_unauthorized_when_request_service_token_is_missing() {
        InternalServiceProperties properties = new InternalServiceProperties();
        properties.setServiceToken("conference-token");
        InternalServiceTokenValidator validator = new InternalServiceTokenValidator(properties);

        ApiException ex = assertThrows(ApiException.class, () -> validator.validate(" "));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("auth.service_token_invalid", ex.getCode());
    }

    @Test
    void should_throw_unauthorized_when_request_service_token_is_invalid() {
        InternalServiceProperties properties = new InternalServiceProperties();
        properties.setServiceToken("conference-token");
        InternalServiceTokenValidator validator = new InternalServiceTokenValidator(properties);

        ApiException ex = assertThrows(ApiException.class, () -> validator.validate("wrong-token"));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("auth.service_token_invalid", ex.getCode());
    }

    @Test
    void should_accept_request_when_service_token_matches_after_trimming() {
        InternalServiceProperties properties = new InternalServiceProperties();
        properties.setServiceToken("conference-token");
        InternalServiceTokenValidator validator = new InternalServiceTokenValidator(properties);

        assertDoesNotThrow(() -> validator.validate(" conference-token "));
    }
}
