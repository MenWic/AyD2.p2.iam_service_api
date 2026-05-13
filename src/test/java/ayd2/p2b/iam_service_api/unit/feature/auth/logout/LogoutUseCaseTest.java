package ayd2.p2b.iam_service_api.unit.feature.auth.logout;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.feature.auth.application.logout.LogoutUseCase;
import ayd2.p2b.iam_service_api.feature.auth.application.port.ParsedToken;
import ayd2.p2b.iam_service_api.feature.auth.application.port.RefreshTokenBlacklistPort;
import ayd2.p2b.iam_service_api.feature.auth.application.port.TokenHashPort;
import ayd2.p2b.iam_service_api.feature.auth.application.port.TokenParserPort;
import ayd2.p2b.iam_service_api.feature.auth.domain.model.TokenType;
import ayd2.p2b.iam_service_api.feature.auth.dto.request.LogoutRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseTest {

    @Mock private RefreshTokenBlacklistPort blacklistPort;
    @Mock private TokenParserPort tokenParserPort;
    @Mock private TokenHashPort tokenHashPort;

    private LogoutUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new LogoutUseCase(blacklistPort, tokenParserPort, tokenHashPort);
    }

    @Test
    void should_blacklist_refresh_token_when_logout_is_requested() {
        UUID userId = UUID.randomUUID();
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("refresh-token");
        ParsedToken token = new ParsedToken(userId, userId.toString(), null, List.of(), TokenType.REFRESH, Instant.now().plusSeconds(600));

        when(tokenParserPort.parseToken("refresh-token", TokenType.REFRESH)).thenReturn(token);
        when(tokenHashPort.sha256("refresh-token")).thenReturn("hash");
        when(blacklistPort.existsByTokenHash("hash")).thenReturn(false);

        useCase.execute(userId, request);

        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<UUID> userCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(blacklistPort).save(hashCaptor.capture(), userCaptor.capture(), org.mockito.ArgumentMatchers.any());
        assertEquals(userId, userCaptor.getValue());
        assertEquals("hash", hashCaptor.getValue());
    }

    @Test
    void should_fail_logout_when_authenticated_user_id_is_null() {
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("refresh-token");

        ApiException exception = assertThrows(ApiException.class, () -> useCase.execute(null, request));

        assertEquals("auth.token_invalid", exception.getCode());
    }

    @Test
    void should_fail_logout_when_request_is_null() {
        ApiException exception = assertThrows(ApiException.class, () -> useCase.execute(UUID.randomUUID(), null));

        assertEquals("auth.token_invalid", exception.getCode());
    }

    @Test
    void should_fail_logout_when_refresh_token_is_blank() {
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("   ");

        ApiException exception = assertThrows(ApiException.class, () -> useCase.execute(UUID.randomUUID(), request));

        assertEquals("auth.token_invalid", exception.getCode());
    }

    @Test
    void should_fail_logout_when_refresh_token_does_not_belong_to_user() {
        UUID authenticatedUserId = UUID.randomUUID();
        UUID tokenUserId = UUID.randomUUID();
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("refresh-token");
        ParsedToken token = new ParsedToken(tokenUserId, tokenUserId.toString(), null, List.of(), TokenType.REFRESH, Instant.now().plusSeconds(600));

        when(tokenParserPort.parseToken("refresh-token", TokenType.REFRESH)).thenReturn(token);

        ApiException exception = assertThrows(ApiException.class, () -> useCase.execute(authenticatedUserId, request));

        assertEquals("auth.token_invalid", exception.getCode());
    }
}

