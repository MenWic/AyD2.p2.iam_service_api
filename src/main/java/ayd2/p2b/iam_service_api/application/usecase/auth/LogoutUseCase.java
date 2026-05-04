package ayd2.p2b.iam_service_api.application.usecase.auth;

import ayd2.p2b.iam_service_api.application.dto.auth.LogoutRequest;
import ayd2.p2b.iam_service_api.application.port.auth.RefreshTokenBlacklistPort;
import ayd2.p2b.iam_service_api.application.port.security.ParsedToken;
import ayd2.p2b.iam_service_api.application.port.security.TokenHashPort;
import ayd2.p2b.iam_service_api.application.port.security.TokenParserPort;
import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.domain.model.auth.TokenType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class LogoutUseCase {

    private final RefreshTokenBlacklistPort blacklistPort;
    private final TokenParserPort tokenParserPort;
    private final TokenHashPort tokenHashPort;

    public LogoutUseCase(
            RefreshTokenBlacklistPort blacklistPort,
            TokenParserPort tokenParserPort,
            TokenHashPort tokenHashPort
    ) {
        this.blacklistPort = blacklistPort;
        this.tokenParserPort = tokenParserPort;
        this.tokenHashPort = tokenHashPort;
    }

    @Transactional
    public void execute(UUID authenticatedUserId, LogoutRequest request) {
        ParsedToken parsedToken = tokenParserPort.parseToken(request.getRefreshToken(), TokenType.REFRESH);
        if (!authenticatedUserId.equals(parsedToken.userId())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "auth.token_invalid", "Refresh token does not belong to user");
        }
        String tokenHash = tokenHashPort.sha256(request.getRefreshToken());
        if (!blacklistPort.existsByTokenHash(tokenHash)) {
            blacklistPort.save(tokenHash, parsedToken.userId(), parsedToken.expiresAt());
        }
    }
}
