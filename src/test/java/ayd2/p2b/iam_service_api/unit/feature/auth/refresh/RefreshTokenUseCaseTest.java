package ayd2.p2b.iam_service_api.unit.feature.auth.refresh;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.feature.auth.application.port.ParsedToken;
import ayd2.p2b.iam_service_api.feature.auth.application.port.RefreshTokenBlacklistPort;
import ayd2.p2b.iam_service_api.feature.auth.application.port.TokenHashPort;
import ayd2.p2b.iam_service_api.feature.auth.application.port.TokenIssuerPort;
import ayd2.p2b.iam_service_api.feature.auth.application.port.TokenParserPort;
import ayd2.p2b.iam_service_api.feature.auth.application.refresh.RefreshTokenUseCase;
import ayd2.p2b.iam_service_api.feature.auth.domain.model.TokenType;
import ayd2.p2b.iam_service_api.feature.auth.dto.request.RefreshRequest;
import ayd2.p2b.iam_service_api.feature.auth.dto.response.RefreshResponse;
import ayd2.p2b.iam_service_api.feature.user.application.port.UserRepositoryPort;
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {

    @Mock private UserRepositoryPort userRepository;
    @Mock private RefreshTokenBlacklistPort blacklistPort;
    @Mock private TokenIssuerPort tokenIssuerPort;
    @Mock private TokenParserPort tokenParserPort;
    @Mock private TokenHashPort tokenHashPort;

    private RefreshTokenUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RefreshTokenUseCase(userRepository, blacklistPort, tokenIssuerPort, tokenParserPort, tokenHashPort);
    }

    @Test
    void should_create_new_access_token_when_refresh_token_is_valid() {
        UUID userId = UUID.randomUUID();
        ParsedToken token = new ParsedToken(userId, userId.toString(), null, List.of(), TokenType.REFRESH, Instant.now().plusSeconds(300));
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("refresh-token");

        when(tokenParserPort.parseToken("refresh-token", TokenType.REFRESH)).thenReturn(token);
        when(tokenHashPort.sha256("refresh-token")).thenReturn("hash");
        when(blacklistPort.existsByTokenHash("hash")).thenReturn(false);
        when(userRepository.findByIdAndActiveTrue(userId)).thenReturn(Optional.of(activeUser(userId)));
        when(tokenIssuerPort.generateAccessToken(any(UserAccount.class))).thenReturn("new-access");

        RefreshResponse response = useCase.execute(request);

        assertEquals("new-access", response.getAccessToken());
    }

    @Test
    void should_fail_refresh_when_refresh_token_is_blacklisted() {
        UUID userId = UUID.randomUUID();
        ParsedToken token = new ParsedToken(userId, userId.toString(), null, List.of(), TokenType.REFRESH, Instant.now().plusSeconds(300));
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("refresh-token");

        when(tokenParserPort.parseToken("refresh-token", TokenType.REFRESH)).thenReturn(token);
        when(tokenHashPort.sha256("refresh-token")).thenReturn("hash");
        when(blacklistPort.existsByTokenHash("hash")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> useCase.execute(request));
        assertEquals("auth.token_invalid", exception.getCode());
    }

    private UserAccount activeUser(UUID userId) {
        return UserAccount.builder()
                .id(userId)
                .email("participant@domain.com")
                .active(true)
                .roles(Set.of(Role.PARTICIPANT))
                .build();
    }
}

