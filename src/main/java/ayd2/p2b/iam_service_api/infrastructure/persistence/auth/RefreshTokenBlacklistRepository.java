package ayd2.p2b.iam_service_api.infrastructure.persistence.auth;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenBlacklistRepository extends JpaRepository<RefreshTokenBlacklistEntity, String> {
}
