package ayd2.p2b.iam_service_api.feature.user.application.port;

import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.dto.internal.UserSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    Optional<UserAccount> findByEmailIgnoreCase(String email);
    Optional<UserAccount> findById(UUID id);
    Optional<UserAccount> findByIdAndActiveTrue(UUID id);
    List<UserAccount> findActiveUsersByPersonalIdIgnoreCase(String personalId);
    Page<UserAccount> findAll(UserSearchCriteria criteria, Pageable pageable);
    long countActiveByRole(Role role);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByPersonalIdIgnoreCase(String personalId);
    UserAccount save(UserAccount userAccount);
}

