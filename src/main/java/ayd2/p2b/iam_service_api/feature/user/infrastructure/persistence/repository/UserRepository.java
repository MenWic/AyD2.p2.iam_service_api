package ayd2.p2b.iam_service_api.feature.user.infrastructure.persistence.repository;

import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {

    Optional<UserEntity> findByEmailIgnoreCase(String email);

    Optional<UserEntity> findByIdAndActiveTrue(UUID id);

    List<UserEntity> findAllByPersonalIdIgnoreCaseAndActiveTrue(String personalId);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPersonalIdIgnoreCase(String personalId);

    @Query("select count(u) from UserEntity u join u.roles r where u.active = true and r = :role")
    long countActiveByRole(@Param("role") Role role);
}

