package ayd2.p2b.iam_service_api.integration.persistence;

import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import ayd2.p2b.iam_service_api.feature.user.dto.internal.UserSearchCriteria;
import ayd2.p2b.iam_service_api.feature.user.infrastructure.persistence.adapter.JpaUserRepositoryAdapter;
import ayd2.p2b.iam_service_api.feature.user.infrastructure.persistence.entity.UserEntity;
import ayd2.p2b.iam_service_api.feature.user.infrastructure.persistence.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaUserRepositoryAdapter.class)
class JpaUserRepositoryAdapterTest extends PostgresDataJpaTestSupport {

    @Autowired
    private JpaUserRepositoryAdapter adapter;

    @Autowired
    private UserRepository userRepository;

    @Test
    void should_preserve_roles_linked_institutions_audit_and_active_fields_on_save_and_read() {
        UUID userId = UUID.randomUUID();
        UUID institutionId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        UUID updatedBy = UUID.randomUUID();

        UserAccount saved = adapter.save(
                userAccount(
                        userId,
                        "persisted@domain.com",
                        "PID100",
                        true,
                        "encoded-password",
                        Set.of(Role.CONGRESS_ADMIN, Role.PARTICIPANT),
                        Set.of(institutionId),
                        createdBy,
                        updatedBy
                )
        );

        UserAccount loaded = adapter.findById(saved.getId()).orElseThrow();

        assertEquals(userId, loaded.getId());
        assertEquals(Set.of(Role.CONGRESS_ADMIN, Role.PARTICIPANT), loaded.getRoles());
        assertEquals(Set.of(institutionId), loaded.getLinkedInstitutions());
        assertEquals(createdBy, loaded.getCreatedBy());
        assertEquals(updatedBy, loaded.getUpdatedBy());
        assertTrue(loaded.getActive());
    }

    @Test
    void should_allow_guest_speaker_with_null_password_hash() {
        UUID userId = UUID.randomUUID();

        UserAccount saved = adapter.save(
                userAccount(
                        userId,
                        "guest@domain.com",
                        "PID101",
                        true,
                        null,
                        Set.of(Role.GUEST_SPEAKER),
                        Set.of(),
                        UUID.randomUUID(),
                        UUID.randomUUID()
                )
        );

        UserAccount loaded = adapter.findById(saved.getId()).orElseThrow();

        assertNull(loaded.getPasswordHash());
        assertEquals(Set.of(Role.GUEST_SPEAKER), loaded.getRoles());
    }

    @Test
    void should_map_find_all_page_from_entity_to_domain() {
        userRepository.saveAndFlush(
                entity("page-1@domain.com", "PID102", true, Set.of(Role.PARTICIPANT), Set.of(), "Page One User")
        );
        userRepository.saveAndFlush(
                entity("page-2@domain.com", "PID103", true, Set.of(Role.CONGRESS_ADMIN), Set.of(UUID.randomUUID()), "Page Two User")
        );

        Page<UserAccount> page = adapter.findAll(UserSearchCriteria.builder().build(), PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());
        assertEquals(1, page.getTotalPages());
        assertEquals(2, page.getContent().size());
        assertNotNull(page.getContent().getFirst().getId());
    }

    @Test
    void should_apply_criteria_filters_in_find_all() {
        UUID includedInstitution = UUID.randomUUID();

        userRepository.saveAndFlush(
                entity("included@domain.com", "PID104", true, Set.of(Role.CONGRESS_ADMIN), Set.of(includedInstitution), "Included User")
        );
        userRepository.saveAndFlush(
                entity("excluded-role@domain.com", "PID105", true, Set.of(Role.PARTICIPANT), Set.of(includedInstitution), "Excluded Role")
        );
        userRepository.saveAndFlush(
                entity("excluded-active@domain.com", "PID106", false, Set.of(Role.CONGRESS_ADMIN), Set.of(includedInstitution), "Excluded Active")
        );

        UserSearchCriteria criteria = UserSearchCriteria.builder()
                .role(Role.CONGRESS_ADMIN)
                .active(true)
                .institutionId(includedInstitution)
                .search("included")
                .build();

        Page<UserAccount> page = adapter.findAll(criteria, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("included@domain.com", page.getContent().getFirst().getEmail());
    }

    @Test
    void should_handle_null_roles_and_null_linked_institutions_as_empty_sets_on_save() {
        UUID userId = UUID.randomUUID();

        UserAccount saved = adapter.save(
                userAccount(
                        userId,
                        "null-collections@domain.com",
                        "PID107",
                        true,
                        "encoded-password",
                        null,
                        null,
                        UUID.randomUUID(),
                        UUID.randomUUID()
                )
        );

        assertTrue(saved.getRoles().isEmpty());
        assertTrue(saved.getLinkedInstitutions().isEmpty());

        UserEntity persisted = userRepository.findById(saved.getId()).orElseThrow();
        assertTrue(persisted.getRoles().isEmpty());
        assertTrue(persisted.getLinkedInstitutions().isEmpty());
    }

    private UserAccount userAccount(
            UUID id,
            String email,
            String personalId,
            boolean active,
            String passwordHash,
            Set<Role> roles,
            Set<UUID> institutions,
            UUID createdBy,
            UUID updatedBy
    ) {
        return UserAccount.builder()
                .id(id)
                .email(email)
                .passwordHash(passwordHash)
                .fullName("Adapter User " + personalId)
                .organization("Code n Bugs")
                .phone("555-0101")
                .personalId(personalId)
                .photoUrl(null)
                .active(active)
                .roles(roles)
                .linkedInstitutions(institutions)
                .createdBy(createdBy)
                .updatedBy(updatedBy)
                .build();
    }

    private UserEntity entity(
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
