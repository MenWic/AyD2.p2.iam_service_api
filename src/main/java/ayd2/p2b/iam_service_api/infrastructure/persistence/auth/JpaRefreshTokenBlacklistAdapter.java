package ayd2.p2b.iam_service_api.infrastructure.persistence.auth;

import ayd2.p2b.iam_service_api.application.port.auth.RefreshTokenBlacklistPort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class JpaRefreshTokenBlacklistAdapter implements RefreshTokenBlacklistPort {

    private final RefreshTokenBlacklistRepository refreshTokenBlacklistRepository;

    public JpaRefreshTokenBlacklistAdapter(RefreshTokenBlacklistRepository refreshTokenBlacklistRepository) {
        this.refreshTokenBlacklistRepository = refreshTokenBlacklistRepository;
    }

    @Override
    public boolean existsByTokenHash(String tokenHash) {
        return refreshTokenBlacklistRepository.existsById(tokenHash);
    }

    @Override
    public void save(String tokenHash, UUID userId, Instant expiresAt) {
        refreshTokenBlacklistRepository.save(RefreshTokenBlacklistEntity.builder()
                .tokenHash(tokenHash)
                .userId(userId)
                .expiresAt(expiresAt)
                .build());
    }
}

