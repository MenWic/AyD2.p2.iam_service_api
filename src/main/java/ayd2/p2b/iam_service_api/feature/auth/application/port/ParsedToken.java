package ayd2.p2b.iam_service_api.feature.auth.application.port;

import ayd2.p2b.iam_service_api.feature.auth.domain.model.TokenType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedToken {
    private UUID userId;
    private String subject;
    private String email;
    private List<String> roles;
    private TokenType tokenType;
    private Instant expiresAt;
}

