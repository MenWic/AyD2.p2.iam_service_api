package ayd2.p2b.iam_service_api.integration.persistence;

import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.infrastructure.persistence.entity.UserEntity;
import ayd2.p2b.iam_service_api.feature.user.infrastructure.persistence.repository.UserRepository;
import ayd2.p2b.iam_service_api.feature.user.infrastructure.persistence.specification.UserSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserSpecificationTest extends PostgresDataJpaTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Test
    void should_filter_by_role() {
        userRepository.saveAndFlush(user("participant@domain.com", "SPEC001", true, Set.of(Role.PARTICIPANT), Set.of()));
        userRepository.saveAndFlush(user("admin@domain.com", "SPEC002", true, Set.of(Role.CONGRESS_ADMIN), Set.of()));

        List<UserEntity> result = userRepository.findAll(UserSpecification.hasRole(Role.CONGRESS_ADMIN));

        assertEquals(1, result.size());
        assertEquals("admin@domain.com", result.getFirst().getEmail());
    }

    @Test
    void should_filter_by_active_state() {
        userRepository.saveAndFlush(user("active@domain.com", "SPEC003", true, Set.of(Role.PARTICIPANT), Set.of()));
        userRepository.saveAndFlush(user("inactive@domain.com", "SPEC004", false, Set.of(Role.PARTICIPANT), Set.of()));

        List<UserEntity> activeUsers = userRepository.findAll(UserSpecification.isActive(true));
        List<UserEntity> inactiveUsers = userRepository.findAll(UserSpecification.isActive(false));

        assertEquals(1, activeUsers.size());
        assertEquals("active@domain.com", activeUsers.getFirst().getEmail());
        assertEquals(1, inactiveUsers.size());
        assertEquals("inactive@domain.com", inactiveUsers.getFirst().getEmail());
    }

    @Test
    void should_filter_by_single_institution() {
        UUID institutionA = UUID.randomUUID();
        UUID institutionB = UUID.randomUUID();

        userRepository.saveAndFlush(user("inst-a@domain.com", "SPEC005", true, Set.of(Role.CONGRESS_ADMIN), Set.of(institutionA)));
        userRepository.saveAndFlush(user("inst-b@domain.com", "SPEC006", true, Set.of(Role.CONGRESS_ADMIN), Set.of(institutionB)));

        List<UserEntity> result = userRepository.findAll(UserSpecification.hasInstitution(institutionA));

        assertEquals(1, result.size());
        assertEquals("inst-a@domain.com", result.getFirst().getEmail());
    }

    @Test
    void should_filter_by_any_institution_scope() {
        UUID institutionA = UUID.randomUUID();
        UUID institutionB = UUID.randomUUID();
        UUID institutionC = UUID.randomUUID();

        userRepository.saveAndFlush(user("scope-a@domain.com", "SPEC007", true, Set.of(Role.CONGRESS_ADMIN), Set.of(institutionA)));
        userRepository.saveAndFlush(user("scope-b@domain.com", "SPEC008", true, Set.of(Role.CONGRESS_ADMIN), Set.of(institutionB)));
        userRepository.saveAndFlush(user("scope-c@domain.com", "SPEC009", true, Set.of(Role.CONGRESS_ADMIN), Set.of(institutionC)));

        List<UserEntity> result = userRepository.findAll(UserSpecification.hasAnyInstitution(Set.of(institutionA, institutionB)));

        assertEquals(2, result.size());
    }

    @Test
    void should_match_search_by_full_name_case_insensitively() {
        userRepository.saveAndFlush(user("name-search@domain.com", "SPEC010", true, Set.of(Role.PARTICIPANT), Set.of(), "Ana Martinez"));

        List<UserEntity> result = userRepository.findAll(UserSpecification.searchMatches("anA mar"));

        assertEquals(1, result.size());
        assertEquals("Ana Martinez", result.getFirst().getFullName());
    }

    @Test
    void should_match_search_by_email_case_insensitively() {
        userRepository.saveAndFlush(user("Search.Email@domain.com", "SPEC011", true, Set.of(Role.PARTICIPANT), Set.of(), "Any User"));

        List<UserEntity> result = userRepository.findAll(UserSpecification.searchMatches("search.email"));

        assertEquals(1, result.size());
        assertEquals("Search.Email@domain.com", result.getFirst().getEmail());
    }

    @Test
    void should_not_duplicate_results_with_combined_role_and_institution_joins() {
        UUID institutionA = UUID.randomUUID();
        UUID institutionB = UUID.randomUUID();

        UserEntity saved = userRepository.saveAndFlush(
                user(
                        "combined@domain.com",
                        "SPEC012",
                        true,
                        Set.of(Role.CONGRESS_ADMIN, Role.PARTICIPANT),
                        Set.of(institutionA, institutionB),
                        "Combined User"
                )
        );

        Specification<UserEntity> specification = Specification
                .where(UserSpecification.hasRole(Role.CONGRESS_ADMIN))
                .and(UserSpecification.hasAnyInstitution(Set.of(institutionA, institutionB)))
                .and(UserSpecification.isActive(true));

        List<UserEntity> result = userRepository.findAll(specification);

        assertEquals(1, result.size());
        assertEquals(saved.getId(), result.getFirst().getId());
    }

    @Test
    void should_treat_null_filters_as_no_op_conjunctions() {
        userRepository.saveAndFlush(user("noop-1@domain.com", "SPEC013", true, Set.of(Role.PARTICIPANT), Set.of()));
        userRepository.saveAndFlush(user("noop-2@domain.com", "SPEC014", false, Set.of(Role.CONGRESS_ADMIN), Set.of(UUID.randomUUID())));

        Specification<UserEntity> noOpSpecification = Specification
                .where(UserSpecification.hasRole(null))
                .and(UserSpecification.isActive(null))
                .and(UserSpecification.hasInstitution(null))
                .and(UserSpecification.hasAnyInstitution(null))
                .and(UserSpecification.searchMatches(null));

        List<UserEntity> result = userRepository.findAll(noOpSpecification);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(entity -> entity.getEmail().equals("noop-1@domain.com")));
        assertTrue(result.stream().anyMatch(entity -> entity.getEmail().equals("noop-2@domain.com")));
    }


    @Test
    void should_match_search_by_personal_id_case_insensitively() {
        userRepository.saveAndFlush(user("personal-search@domain.com", "SpecPid015", true, Set.of(Role.PARTICIPANT), Set.of(), "Personal Search User"));

        List<UserEntity> result = userRepository.findAll(UserSpecification.searchMatches("specpid"));

        assertEquals(1, result.size());
        assertEquals("SpecPid015", result.getFirst().getPersonalId());
    }

    @Test
    void should_apply_composed_role_active_institution_and_search_filters() {
        UUID institutionA = UUID.randomUUID();
        UUID institutionB = UUID.randomUUID();

        userRepository.saveAndFlush(user("target-filter@domain.com", "SPEC016", true, Set.of(Role.CONGRESS_ADMIN), Set.of(institutionA), "Target Filter User"));
        userRepository.saveAndFlush(user("wrong-role@domain.com", "SPEC017", true, Set.of(Role.PARTICIPANT), Set.of(institutionA), "Target Filter User"));
        userRepository.saveAndFlush(user("wrong-active@domain.com", "SPEC018", false, Set.of(Role.CONGRESS_ADMIN), Set.of(institutionA), "Target Filter User"));
        userRepository.saveAndFlush(user("wrong-institution@domain.com", "SPEC019", true, Set.of(Role.CONGRESS_ADMIN), Set.of(institutionB), "Target Filter User"));
        userRepository.saveAndFlush(user("wrong-search@domain.com", "SPEC020", true, Set.of(Role.CONGRESS_ADMIN), Set.of(institutionA), "Different User"));

        Specification<UserEntity> specification = Specification
                .where(UserSpecification.hasRole(Role.CONGRESS_ADMIN))
                .and(UserSpecification.isActive(true))
                .and(UserSpecification.hasInstitution(institutionA))
                .and(UserSpecification.searchMatches("target filter"));

        List<UserEntity> result = userRepository.findAll(specification);

        assertEquals(1, result.size());
        assertEquals("target-filter@domain.com", result.getFirst().getEmail());
    }

    @Test
    void should_treat_empty_institution_scope_as_no_op_conjunction() {
        userRepository.saveAndFlush(user("empty-scope-1@domain.com", "SPEC021", true, Set.of(Role.PARTICIPANT), Set.of()));
        userRepository.saveAndFlush(user("empty-scope-2@domain.com", "SPEC022", true, Set.of(Role.CONGRESS_ADMIN), Set.of(UUID.randomUUID())));

        List<UserEntity> result = userRepository.findAll(UserSpecification.hasAnyInstitution(Set.of()));

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(entity -> entity.getEmail().equals("empty-scope-1@domain.com")));
        assertTrue(result.stream().anyMatch(entity -> entity.getEmail().equals("empty-scope-2@domain.com")));
    }

    private UserEntity user(String email, String personalId, boolean active, Set<Role> roles, Set<UUID> institutions) {
        return user(email, personalId, active, roles, institutions, "Spec User " + personalId);
    }

    private UserEntity user(
            String email,
            String personalId,
            boolean active,
            Set<Role> roles,
            Set<UUID> institutions,
            String fullName
    ) {
        return UserEntity.builder()
                .email(email)
                .passwordHash("encoded-password")
                .fullName(fullName)
                .organization("Code n Bugs")
                .phone("555-0101")
                .personalId(personalId)
                .photoUrl(null)
                .active(active)
                .roles(roles)
                .linkedInstitutions(institutions)
                .createdBy(UUID.randomUUID())
                .updatedBy(UUID.randomUUID())
                .build();
    }
}
