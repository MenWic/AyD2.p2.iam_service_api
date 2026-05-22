package ayd2.p2b.iam_service_api.unit.feature.auth.security.token;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.feature.auth.application.port.ParsedToken;
import ayd2.p2b.iam_service_api.feature.auth.domain.model.TokenType;
import ayd2.p2b.iam_service_api.feature.auth.infrastructure.security.token.JwtProperties;
import ayd2.p2b.iam_service_api.feature.auth.infrastructure.security.token.JwtTokenAdapter;
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenAdapterTest {

    private JwtTokenAdapter jwtTokenAdapter;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("change_me_super_secret_key_at_least_32_chars_long");
        properties.setExpirationMinutes(15);
        properties.setRefreshExpirationDays(7);
        jwtTokenAdapter = new JwtTokenAdapter(properties);
    }

    @Test
    void should_include_fullName_claim_in_access_token() {
        UserAccount user = UserAccount.builder()
                .id(UUID.randomUUID())
                .email("user@domain.com")
                .fullName("Jane Doe")
                .roles(Set.of(Role.PARTICIPANT))
                .build();

        String accessToken = jwtTokenAdapter.generateAccessToken(user);
        ParsedToken parsedToken = jwtTokenAdapter.parseToken(accessToken, TokenType.ACCESS);

        assertEquals("Jane Doe", parsedToken.getFullName());
    }

    @Test
    void should_include_roles_list_when_access_token_is_generated() {
        UserAccount user = UserAccount.builder()
                .id(UUID.randomUUID())
                .email("participant@domain.com")
                .roles(Set.of(Role.PARTICIPANT, Role.CONGRESS_ADMIN))
                .build();

        String accessToken = jwtTokenAdapter.generateAccessToken(user);
        ParsedToken parsedToken = jwtTokenAdapter.parseToken(accessToken, TokenType.ACCESS);

        assertTrue(parsedToken.getRoles().contains("PARTICIPANT"));
        assertTrue(parsedToken.getRoles().contains("CONGRESS_ADMIN"));
    }

    @Test
    void should_reject_token_when_refresh_token_is_validated_as_access() {
        UserAccount user = UserAccount.builder()
                .id(UUID.randomUUID())
                .email("participant@domain.com")
                .roles(Set.of(Role.PARTICIPANT))
                .build();

        String refreshToken = jwtTokenAdapter.generateRefreshToken(user);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> jwtTokenAdapter.parseToken(refreshToken, TokenType.ACCESS));
        assertEquals("auth.token_invalid", exception.getCode());
    }

    @Test
    void should_parse_access_token_with_identity_claims_and_expiration_when_token_is_valid() {
        UUID userId = UUID.randomUUID();
        UserAccount user = UserAccount.builder()
                .id(userId)
                .email("user@domain.com")
                .fullName("Jane Doe")
                .roles(Set.of(Role.PARTICIPANT))
                .build();

        String accessToken = jwtTokenAdapter.generateAccessToken(user);
        ParsedToken parsedToken = jwtTokenAdapter.parseToken(accessToken, TokenType.ACCESS);

        assertEquals(userId, parsedToken.getUserId());
        assertEquals(userId.toString(), parsedToken.getSubject());
        assertEquals("user@domain.com", parsedToken.getEmail());
        assertEquals("Jane Doe", parsedToken.getFullName());
        assertEquals(List.of(Role.PARTICIPANT.name()), parsedToken.getRoles());
        assertEquals(TokenType.ACCESS, parsedToken.getTokenType());
        assertNotNull(parsedToken.getExpiresAt());
    }

    @Test
    void should_parse_refresh_token_with_refresh_type_and_expiration_when_token_is_valid() {
        UUID userId = UUID.randomUUID();
        UserAccount user = UserAccount.builder()
                .id(userId)
                .email("user@domain.com")
                .roles(Set.of(Role.PARTICIPANT))
                .build();

        String refreshToken = jwtTokenAdapter.generateRefreshToken(user);
        ParsedToken parsedToken = jwtTokenAdapter.parseToken(refreshToken, TokenType.REFRESH);

        assertEquals(userId, parsedToken.getUserId());
        assertEquals(userId.toString(), parsedToken.getSubject());
        assertEquals(TokenType.REFRESH, parsedToken.getTokenType());
        assertNotNull(parsedToken.getExpiresAt());
    }

    @Test
    void should_reject_malformed_token_when_parsing() {
        ApiException exception = assertThrows(
                ApiException.class,
                () -> jwtTokenAdapter.parseToken("not-a-jwt", TokenType.ACCESS));

        assertEquals("auth.token_invalid", exception.getCode());
    }

    @Test
    void should_reject_token_when_signed_with_different_secret() {
        JwtTokenAdapter issuer = buildAdapter("issuer-secret-which-is-at-least-32-characters-long", 15, 7);
        JwtTokenAdapter parser = buildAdapter("parser-secret-which-is-at-least-32-characters-long", 15, 7);

        UserAccount user = UserAccount.builder()
                .id(UUID.randomUUID())
                .email("user@domain.com")
                .roles(Set.of(Role.PARTICIPANT))
                .build();

        String accessToken = issuer.generateAccessToken(user);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> parser.parseToken(accessToken, TokenType.ACCESS));

        assertEquals("auth.token_invalid", exception.getCode());
    }

    @Test
    void should_reject_expired_token_when_parsing() {
        JwtTokenAdapter expiredAdapter = buildAdapter("expired-secret-which-is-at-least-32-characters-long", -1, 7);
        UserAccount user = UserAccount.builder()
                .id(UUID.randomUUID())
                .email("user@domain.com")
                .roles(Set.of(Role.PARTICIPANT))
                .build();

        String accessToken = expiredAdapter.generateAccessToken(user);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> expiredAdapter.parseToken(accessToken, TokenType.ACCESS));

        assertEquals("auth.token_expired", exception.getCode());
    }

    @Test
    void should_reject_access_token_when_refresh_token_is_expected() {
        UserAccount user = UserAccount.builder()
                .id(UUID.randomUUID())
                .email("participant@domain.com")
                .roles(Set.of(Role.PARTICIPANT))
                .build();

        String accessToken = jwtTokenAdapter.generateAccessToken(user);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> jwtTokenAdapter.parseToken(accessToken, TokenType.REFRESH));

        assertEquals("auth.token_invalid", exception.getCode());
    }

    @Test
    void should_parse_empty_roles_when_user_roles_are_null() {
        UserAccount user = UserAccount.builder()
                .id(UUID.randomUUID())
                .email("participant@domain.com")
                .roles(null)
                .build();

        String accessToken = jwtTokenAdapter.generateAccessToken(user);
        ParsedToken parsedToken = jwtTokenAdapter.parseToken(accessToken, TokenType.ACCESS);

        assertTrue(parsedToken.getRoles().isEmpty());
    }

    private JwtTokenAdapter buildAdapter(String secret, int expirationMinutes, int refreshExpirationDays) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(secret);
        properties.setExpirationMinutes(expirationMinutes);
        properties.setRefreshExpirationDays(refreshExpirationDays);
        return new JwtTokenAdapter(properties);
    }
}
