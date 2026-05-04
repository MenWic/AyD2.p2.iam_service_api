package ayd2.p2b.iam_service_api.unit.usecase.auth;

import ayd2.p2b.iam_service_api.application.dto.auth.LogoutRequest;
import ayd2.p2b.iam_service_api.application.port.auth.RefreshTokenBlacklistPort;
import ayd2.p2b.iam_service_api.application.port.security.ParsedToken;
import ayd2.p2b.iam_service_api.application.port.security.TokenHashPort;
import ayd2.p2b.iam_service_api.application.port.security.TokenParserPort;
import ayd2.p2b.iam_service_api.application.usecase.auth.LogoutUseCase;
import ayd2.p2b.iam_service_api.domain.model.auth.TokenType;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseTest {

    @Mock
    private RefreshTokenBlacklistPort blacklistPort;
    @Mock
    private TokenParserPort tokenParserPort;
    @Mock
    private TokenHashPort tokenHashPort;

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
}
