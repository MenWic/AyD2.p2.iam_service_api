package ayd2.p2b.iam_service_api.feature.auth.infrastructure.persistence.adapter;

import ayd2.p2b.iam_service_api.feature.auth.application.port.RefreshTokenBlacklistPort;
import ayd2.p2b.iam_service_api.feature.auth.infrastructure.persistence.entity.RefreshTokenBlacklistEntity;
import ayd2.p2b.iam_service_api.feature.auth.infrastructure.persistence.repository.RefreshTokenBlacklistRepository;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JpaRefreshTokenBlacklistAdapter implements RefreshTokenBlacklistPort {

    private final RefreshTokenBlacklistRepository repository;

    @Override
    public boolean existsByTokenHash(String tokenHash) {
        return repository.existsByTokenHash(tokenHash);
    }

    @Override
    public void save(String tokenHash, UUID userId, Instant expiresAt) {
        RefreshTokenBlacklistEntity entity = RefreshTokenBlacklistEntity.builder()
                .tokenHash(tokenHash)
                .userId(userId)
                .expiresAt(expiresAt)
                .build();
        repository.save(entity);
    }
}
