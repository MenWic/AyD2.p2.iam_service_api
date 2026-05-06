package ayd2.p2b.iam_service_api.feature.user.infrastructure.persistence.repository;

import ayd2.p2b.iam_service_api.feature.user.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<ayd2.p2b.iam_service_api.feature.user.infrastructure.persistence.entity.UserEntity> findByEmailIgnoreCase(String email);

    Optional<ayd2.p2b.iam_service_api.feature.user.infrastructure.persistence.entity.UserEntity> findByIdAndActiveTrue(UUID id);

    boolean existsByEmailIgnoreCase(String email);
}

