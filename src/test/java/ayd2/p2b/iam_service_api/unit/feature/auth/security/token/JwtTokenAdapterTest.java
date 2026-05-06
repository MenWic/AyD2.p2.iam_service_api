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

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void should_include_roles_list_when_access_token_is_generated() {
        UserAccount user = UserAccount.builder()
                .id(UUID.randomUUID())
                .email("participant@domain.com")
                .roles(Set.of(Role.PARTICIPANT, Role.CONGRESS_ADMIN))
                .build();

        String accessToken = jwtTokenAdapter.generateAccessToken(user);
        ParsedToken parsedToken = jwtTokenAdapter.parseToken(accessToken, TokenType.ACCESS);

        assertTrue(parsedToken.roles().contains("PARTICIPANT"));
        assertTrue(parsedToken.roles().contains("CONGRESS_ADMIN"));
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
                () -> jwtTokenAdapter.parseToken(refreshToken, TokenType.ACCESS)
        );
        assertEquals("auth.token_invalid", exception.getCode());
    }
}

