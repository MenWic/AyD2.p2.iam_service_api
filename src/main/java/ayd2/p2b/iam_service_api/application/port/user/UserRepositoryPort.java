package ayd2.p2b.iam_service_api.application.port.user;

import ayd2.p2b.iam_service_api.domain.model.user.UserAccount;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

    Optional<UserAccount> findByEmailIgnoreCase(String email);

    Optional<UserAccount> findByIdAndActiveTrue(UUID id);

    boolean existsByEmailIgnoreCase(String email);

    UserAccount save(UserAccount userAccount);
}

