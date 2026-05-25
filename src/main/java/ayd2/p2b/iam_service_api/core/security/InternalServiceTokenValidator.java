package ayd2.p2b.iam_service_api.core.security;

import ayd2.p2b.iam_service_api.common.util.TextNormalizer;
import ayd2.p2b.iam_service_api.feature.auth.application.exception.AuthExceptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InternalServiceTokenValidator {

    private final InternalServiceProperties properties;

    public void validate(String serviceToken) {
        String expectedToken = TextNormalizer.trimToNull(properties.getServiceToken());
        String receivedToken = TextNormalizer.trimToNull(serviceToken);

        if (expectedToken == null || receivedToken == null || !expectedToken.equals(receivedToken)) {
            throw AuthExceptions.serviceTokenInvalid();
        }
    }
}
