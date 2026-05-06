package ayd2.p2b.iam_service_api.feature.auth.application.port;

import ayd2.p2b.iam_service_api.feature.auth.domain.model.TokenType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ParsedToken(
        UUID userId,
        String subject,
        String email,
        List<String> roles,
        TokenType tokenType,
        Instant expiresAt
) {
}

