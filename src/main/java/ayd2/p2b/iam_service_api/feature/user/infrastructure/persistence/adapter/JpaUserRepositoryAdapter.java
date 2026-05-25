package ayd2.p2b.iam_service_api.feature.user.infrastructure.persistence.adapter;

import ayd2.p2b.iam_service_api.feature.user.application.port.UserRepositoryPort;
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import ayd2.p2b.iam_service_api.feature.user.dto.internal.UserSearchCriteria;
import ayd2.p2b.iam_service_api.feature.user.infrastructure.persistence.entity.UserEntity;
import ayd2.p2b.iam_service_api.feature.user.infrastructure.persistence.repository.UserRepository;
import ayd2.p2b.iam_service_api.feature.user.infrastructure.persistence.specification.UserSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JpaUserRepositoryAdapter implements UserRepositoryPort {

    private final UserRepository userRepository;

    @Override
    public Optional<UserAccount> findByEmailIgnoreCase(String email) {
        return userRepository.findByEmailIgnoreCase(email).map(this::toDomain);
    }

    @Override
    public Optional<UserAccount> findById(UUID id) {
        return userRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<UserAccount> findByIdAndActiveTrue(UUID id) {
        return userRepository.findByIdAndActiveTrue(id).map(this::toDomain);
    }

    @Override
    public List<UserAccount> findActiveUsersByPersonalIdIgnoreCase(String personalId) {
        return userRepository.findAllByPersonalIdIgnoreCaseAndActiveTrue(personalId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Page<UserAccount> findAll(UserSearchCriteria criteria, Pageable pageable) {
        UserSearchCriteria effectiveCriteria = criteria == null ? UserSearchCriteria.builder().build() : criteria;
        Specification<UserEntity> specification = Specification
                .where(UserSpecification.hasRole(effectiveCriteria.getRole()))
                .and(UserSpecification.isActive(effectiveCriteria.getActive()))
                .and(UserSpecification.hasInstitution(effectiveCriteria.getInstitutionId()))
                .and(UserSpecification.hasAnyInstitution(effectiveCriteria.getScopedInstitutionIds()))
                .and(UserSpecification.searchMatches(effectiveCriteria.getSearch()));

        return userRepository.findAll(specification, pageable).map(this::toDomain);
    }

    @Override
    public long countActiveByRole(Role role) {
        return userRepository.countActiveByRole(role);
    }

    @Override
    public boolean existsByEmailIgnoreCase(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public boolean existsByPersonalIdIgnoreCase(String personalId) {
        return userRepository.existsByPersonalIdIgnoreCase(personalId);
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
                .roles(entity.getRoles() == null ? Set.of() : Set.copyOf(entity.getRoles()))
                .linkedInstitutions(entity.getLinkedInstitutions() == null ? Set.of() : Set.copyOf(entity.getLinkedInstitutions()))
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
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
                .roles(userAccount.getRoles() == null ? new HashSet<>() : new HashSet<>(userAccount.getRoles()))
                .linkedInstitutions(userAccount.getLinkedInstitutions() == null ? new HashSet<>() : new HashSet<>(userAccount.getLinkedInstitutions()))
                .createdBy(userAccount.getCreatedBy())
                .updatedBy(userAccount.getUpdatedBy())
                .build();
    }
}
