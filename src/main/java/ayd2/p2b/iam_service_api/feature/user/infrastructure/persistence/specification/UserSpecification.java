package ayd2.p2b.iam_service_api.feature.user.infrastructure.persistence.specification;

import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<UserEntity> hasRole(Role role) {
        return (root, query, criteriaBuilder) -> {
            if (role == null) {
                return criteriaBuilder.conjunction();
            }
            Join<UserEntity, Role> rolesJoin = root.joinSet("roles", JoinType.INNER);
            if (query != null) {
                query.distinct(true);
            }
            return criteriaBuilder.equal(rolesJoin, role);
        };
    }

    public static Specification<UserEntity> isActive(Boolean active) {
        return (root, query, criteriaBuilder) -> {
            if (active == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("active"), active);
        };
    }

    public static Specification<UserEntity> hasInstitution(UUID institutionId) {
        return (root, query, criteriaBuilder) -> {
            if (institutionId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<UserEntity, UUID> institutionsJoin = root.joinSet("linkedInstitutions", JoinType.INNER);
            if (query != null) {
                query.distinct(true);
            }
            return criteriaBuilder.equal(institutionsJoin, institutionId);
        };
    }

    public static Specification<UserEntity> hasAnyInstitution(Set<UUID> institutionIds) {
        return (root, query, criteriaBuilder) -> {
            if (institutionIds == null || institutionIds.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Join<UserEntity, UUID> institutionsJoin = root.joinSet("linkedInstitutions", JoinType.INNER);
            if (query != null) {
                query.distinct(true);
            }
            return institutionsJoin.in(institutionIds);
        };
    }

    public static Specification<UserEntity> searchMatches(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern)
            );
        };
    }
}
