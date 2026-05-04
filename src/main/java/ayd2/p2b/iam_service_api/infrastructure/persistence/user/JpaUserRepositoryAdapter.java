package ayd2.p2b.iam_service_api.infrastructure.persistence.user;

import ayd2.p2b.iam_service_api.application.port.user.UserRepositoryPort;
import ayd2.p2b.iam_service_api.domain.model.user.UserAccount;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class JpaUserRepositoryAdapter implements UserRepositoryPort {

    private final UserRepository userRepository;

    public JpaUserRepositoryAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<UserAccount> findByEmailIgnoreCase(String email) {
        return userRepository.findByEmailIgnoreCase(email).map(this::toDomain);
    }

    @Override
    public Optional<UserAccount> findByIdAndActiveTrue(UUID id) {
        return userRepository.findByIdAndActiveTrue(id).map(this::toDomain);
    }

    @Override
    public boolean existsByEmailIgnoreCase(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public UserAccount save(UserAccount userAccount) {
        UserEntity saved = userRepository.save(toEntity(userAccount));
        return toDomain(saved);
    }

    private UserAccount toDomain(UserEntity entity) {
        return UserAccount.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .fullName(entity.getFullName())
                .organization(entity.getOrganization())
                .phone(entity.getPhone())
                .personalId(entity.getPersonalId())
                .photoUrl(entity.getPhotoUrl())
                .active(entity.getActive())
                .roles(entity.getRoles())
                .build();
    }

    private UserEntity toEntity(UserAccount userAccount) {
        return UserEntity.builder()
                .id(userAccount.getId())
                .email(userAccount.getEmail())
                .passwordHash(userAccount.getPasswordHash())
                .fullName(userAccount.getFullName())
                .organization(userAccount.getOrganization())
                .phone(userAccount.getPhone())
                .personalId(userAccount.getPersonalId())
                .photoUrl(userAccount.getPhotoUrl())
                .active(userAccount.getActive())
                .roles(userAccount.getRoles() == null ? Set.of() : userAccount.getRoles())
                .build();
    }
}
