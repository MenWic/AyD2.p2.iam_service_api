package ayd2.p2b.iam_service_api.application.port.auth;

import java.time.Instant;
import java.util.UUID;

public interface RefreshTokenBlacklistPort {

    boolean existsByTokenHash(String tokenHash);

    void save(String tokenHash, UUID userId, Instant expiresAt);
}

