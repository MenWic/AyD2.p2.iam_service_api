package ayd2.p2b.iam_service_api.feature.user.application.port;

import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    Optional<UserAccount> findByEmailIgnoreCase(String email);
    Optional<UserAccount> findByIdAndActiveTrue(UUID id);
    boolean existsByEmailIgnoreCase(String email);
    UserAccount save(UserAccount userAccount);
}

