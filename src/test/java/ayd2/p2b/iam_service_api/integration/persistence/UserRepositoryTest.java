package ayd2.p2b.iam_service_api.integration.persistence;

import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.infrastructure.persistence.entity.UserEntity;
import ayd2.p2b.iam_service_api.feature.user.infrastructure.persistence.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void should_reject_non_alphanumeric_personal_id_by_db_constraint() {
        UserEntity invalid = user("invalid-pid@domain.com", "A-123", true, Set.of(Role.PARTICIPANT), Set.of());

        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(invalid));
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
