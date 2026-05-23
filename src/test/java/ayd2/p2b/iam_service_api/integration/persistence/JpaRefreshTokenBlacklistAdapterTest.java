package ayd2.p2b.iam_service_api.integration.persistence;

import ayd2.p2b.iam_service_api.feature.auth.infrastructure.persistence.adapter.JpaRefreshTokenBlacklistAdapter;
import ayd2.p2b.iam_service_api.feature.auth.infrastructure.persistence.entity.RefreshTokenBlacklistEntity;
import ayd2.p2b.iam_service_api.feature.auth.infrastructure.persistence.repository.RefreshTokenBlacklistRepository;
import ayd2.p2b.iam_service_api.feature.user.infrastructure.persistence.entity.UserEntity;
import ayd2.p2b.iam_service_api.feature.user.infrastructure.persistence.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaRefreshTokenBlacklistAdapter.class)
class JpaRefreshTokenBlacklistAdapterTest extends PostgresDataJpaTestSupport {

    @Autowired
    private JpaRefreshTokenBlacklistAdapter adapter;

    @Autowired
    private RefreshTokenBlacklistRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void should_save_refresh_token_blacklist_entry_with_user_and_expiration() {
        String tokenHash = "a".repeat(64);
        UUID userId = persistUser("a@test.com");
        Instant expiresAt = Instant.now().plusSeconds(3600);

        adapter.save(tokenHash, userId, expiresAt);

        RefreshTokenBlacklistEntity persisted = repository.findByTokenHash(tokenHash).orElseThrow();
        assertEquals(tokenHash, persisted.getTokenHash());
        assertEquals(userId, persisted.getUserId());
        assertEquals(expiresAt, persisted.getExpiresAt());
    }

    @Test
    void should_return_true_when_token_hash_exists() {
        String tokenHash = "b".repeat(64);
        UUID userId = persistUser("b@test.com");
        repository.saveAndFlush(entity(tokenHash, userId));

        assertTrue(adapter.existsByTokenHash(tokenHash));
    }

    @Test
    void should_return_false_when_token_hash_does_not_exist() {
        assertFalse(adapter.existsByTokenHash("c".repeat(64)));
    }

    @Test
    void should_set_created_at_on_persist() {
        String tokenHash = "d".repeat(64);
        UUID userId = persistUser("d@test.com");
        adapter.save(tokenHash, userId, Instant.now().plusSeconds(3600));

        RefreshTokenBlacklistEntity persisted = repository.findById(tokenHash).orElseThrow();
        assertNotNull(persisted.getCreatedAt());
    }

    @Test
    void should_use_token_hash_as_entity_identifier() {
        String tokenHash = "e".repeat(64);
        UUID userId = persistUser("e@test.com");
        repository.saveAndFlush(entity(tokenHash, userId));

        assertTrue(repository.findById(tokenHash).isPresent());
    }

    private UUID persistUser(String email) {
        UserEntity user = UserEntity.builder()
                .email(email)
                .fullName("Test User")
                .organization("Test Org")
                .phone("00000000")
                .personalId("TEST" + email.charAt(0))
                .build();
        return userRepository.saveAndFlush(user).getId();
    }

    private RefreshTokenBlacklistEntity entity(String tokenHash, UUID userId) {
        return RefreshTokenBlacklistEntity.builder()
                .tokenHash(tokenHash)
                .userId(userId)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }
}
