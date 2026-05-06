package ayd2.p2b.iam_service_api.feature.auth.infrastructure.persistence.repository;

import ayd2.p2b.iam_service_api.feature.auth.infrastructure.persistence.entity.RefreshTokenBlacklistEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenBlacklistRepository extends JpaRepository<ayd2.p2b.iam_service_api.feature.auth.infrastructure.persistence.entity.RefreshTokenBlacklistEntity, String> {

    Optional<RefreshTokenBlacklistEntity> findByTokenHash(String tokenHash);

    boolean existsByTokenHash(String tokenHash);
}

