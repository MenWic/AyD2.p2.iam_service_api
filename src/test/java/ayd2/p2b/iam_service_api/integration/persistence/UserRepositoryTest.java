package ayd2.p2b.iam_service_api.integration.persistence;

import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.infrastructure.persistence.entity.UserEntity;
import ayd2.p2b.iam_service_api.feature.user.infrastructure.persistence.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest extends PostgresDataJpaTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Test
    void should_detect_existing_email_ignoring_case() {
        userRepository.saveAndFlush(user("CaseUser@domain.com", "PIDA001", true, Set.of(Role.PARTICIPANT), Set.of()));

        assertTrue(userRepository.existsByEmailIgnoreCase("caseuser@domain.com"));
        assertTrue(userRepository.existsByEmailIgnoreCase("CASEUSER@DOMAIN.COM"));
    }

    @Test
    void should_detect_existing_personal_id_ignoring_case() {
        userRepository.saveAndFlush(user("pid-user@domain.com", "AaBb001", true, Set.of(Role.PARTICIPANT), Set.of()));

        assertTrue(userRepository.existsByPersonalIdIgnoreCase("aabb001"));
        assertTrue(userRepository.existsByPersonalIdIgnoreCase("AABB001"));
    }

    @Test
    void should_count_only_active_users_for_requested_role() {
        userRepository.saveAndFlush(user("active-admin@domain.com", "ADM001", true, Set.of(Role.SYSTEM_ADMIN), Set.of()));
        userRepository.saveAndFlush(user("inactive-admin@domain.com", "ADM002", false, Set.of(Role.SYSTEM_ADMIN), Set.of()));
        userRepository.saveAndFlush(user("active-participant@domain.com", "PAR001", true, Set.of(Role.PARTICIPANT), Set.of()));

        long activeSystemAdmins = userRepository.countActiveByRole(Role.SYSTEM_ADMIN);
        long activeParticipants = userRepository.countActiveByRole(Role.PARTICIPANT);

        assertEquals(1L, activeSystemAdmins);
        assertEquals(1L, activeParticipants);
    }

    @Test
    void should_find_user_by_id_and_active_true_when_active() {
        UserEntity saved = userRepository.saveAndFlush(user("active@domain.com", "ACT001", true, Set.of(Role.PARTICIPANT), Set.of()));

        assertTrue(userRepository.findByIdAndActiveTrue(saved.getId()).isPresent());
    }

    @Test
    void should_not_find_user_by_id_and_active_true_when_inactive() {
        UserEntity saved = userRepository.saveAndFlush(user("inactive@domain.com", "INA001", false, Set.of(Role.PARTICIPANT), Set.of()));

        assertFalse(userRepository.findByIdAndActiveTrue(saved.getId()).isPresent());
    }

    @Test
    void should_find_active_users_by_personal_id_ignoring_case() {
        UserEntity active = userRepository.saveAndFlush(user("active-lookup@domain.com", "CasePid001", true, Set.of(Role.PARTICIPANT), Set.of()));
        userRepository.saveAndFlush(user("inactive-lookup@domain.com", "CASEPID001", false, Set.of(Role.PARTICIPANT), Set.of()));

        List<UserEntity> result = userRepository.findAllByPersonalIdIgnoreCaseAndActiveTrue("casepid001");

        assertEquals(1, result.size());
        assertEquals(active.getId(), result.getFirst().getId());
    }

    @Test
    void should_not_return_inactive_users_when_searching_active_users_by_personal_id() {
        userRepository.saveAndFlush(user("inactive-only@domain.com", "Inactive001", false, Set.of(Role.PARTICIPANT), Set.of()));

        List<UserEntity> result = userRepository.findAllByPersonalIdIgnoreCaseAndActiveTrue("inactive001");

        assertTrue(result.isEmpty());
    }

    @Test
    void should_return_multiple_active_rows_for_case_variant_personal_id_duplicates() {
        UserEntity first = userRepository.saveAndFlush(user("dup-case-1@domain.com", "DupCase001", true, Set.of(Role.PARTICIPANT), Set.of()));
        UserEntity second = userRepository.saveAndFlush(user("dup-case-2@domain.com", "dupCASE001", true, Set.of(Role.PARTICIPANT), Set.of()));

        List<UserEntity> result = userRepository.findAllByPersonalIdIgnoreCaseAndActiveTrue("DUPCASE001");

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(user -> user.getId().equals(first.getId())));
        assertTrue(result.stream().anyMatch(user -> user.getId().equals(second.getId())));
    }

    @Test
    void should_reject_non_alphanumeric_personal_id_by_db_constraint() {
        UserEntity invalid = user("invalid-pid@domain.com", "A-123", true, Set.of(Role.PARTICIPANT), Set.of());

        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(invalid));
    }


    @Test
    void should_document_current_db_allows_case_variant_email_duplicates() {
        userRepository.saveAndFlush(user("CaseVariant@domain.com", "CASEVAR001", true, Set.of(Role.PARTICIPANT), Set.of()));

        assertDoesNotThrow(() -> userRepository.saveAndFlush(
                user("casevariant@domain.com", "CASEVAR002", true, Set.of(Role.PARTICIPANT), Set.of())));
        assertTrue(userRepository.existsByEmailIgnoreCase("CASEVARIANT@DOMAIN.COM"));
    }

    @Test
    void should_document_current_db_allows_case_variant_personal_id_duplicates() {
        userRepository.saveAndFlush(user("case-pid-1@domain.com", "CasePid001", true, Set.of(Role.PARTICIPANT), Set.of()));

        assertDoesNotThrow(() -> userRepository.saveAndFlush(
                user("case-pid-2@domain.com", "casepid001", true, Set.of(Role.PARTICIPANT), Set.of())));
        assertTrue(userRepository.existsByPersonalIdIgnoreCase("CASEPID001"));
    }

    @Test
    void should_apply_pre_persist_defaults_for_id_active_and_timestamps() {
        UserEntity saved = userRepository.saveAndFlush(userWithoutPersistenceDefaults("defaults@domain.com", "DEF001"));

        assertNotNull(saved.getId());
        assertTrue(saved.getActive());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void should_update_updated_at_on_entity_update() throws InterruptedException {
        UserEntity saved = userRepository.saveAndFlush(user("updated-at@domain.com", "UPD001", true, Set.of(Role.PARTICIPANT), Set.of()));
        Instant initialUpdatedAt = saved.getUpdatedAt();
        Thread.sleep(5);

        saved.setFullName("Updated User Name");
        UserEntity updated = userRepository.saveAndFlush(saved);

        assertNotEquals(initialUpdatedAt, updated.getUpdatedAt());
        assertTrue(updated.getUpdatedAt().isAfter(initialUpdatedAt));
    }


    private UserEntity userWithoutPersistenceDefaults(String email, String personalId) {
        return UserEntity.builder()
                .email(email)
                .passwordHash("encoded-password")
                .fullName("Test User " + personalId)
                .organization("Code n Bugs")
                .phone("555-0101")
                .personalId(personalId)
                .roles(Set.of(Role.PARTICIPANT))
                .linkedInstitutions(Set.of())
                .createdBy(UUID.randomUUID())
                .updatedBy(UUID.randomUUID())
                .build();
    }

    private UserEntity user(String email, String personalId, boolean active, Set<Role> roles, Set<UUID> institutions) {
        return UserEntity.builder()
                .email(email)
                .passwordHash("encoded-password")
                .fullName("Test User " + personalId)
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
