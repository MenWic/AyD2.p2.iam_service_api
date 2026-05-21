package ayd2.p2b.iam_service_api.feature.auth.application.logout;

import ayd2.p2b.iam_service_api.feature.auth.application.exception.AuthExceptions;
import ayd2.p2b.iam_service_api.feature.auth.dto.request.LogoutRequest;
import ayd2.p2b.iam_service_api.feature.auth.application.port.RefreshTokenBlacklistPort;
import ayd2.p2b.iam_service_api.feature.auth.application.port.ParsedToken;
import ayd2.p2b.iam_service_api.feature.auth.application.port.TokenHashPort;
import ayd2.p2b.iam_service_api.feature.auth.application.port.TokenParserPort;
import ayd2.p2b.iam_service_api.feature.auth.domain.model.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LogoutUseCase {

    private final RefreshTokenBlacklistPort blacklistPort;
    private final TokenParserPort tokenParserPort;
    private final TokenHashPort tokenHashPort;


    @Transactional
    public void execute(UUID authenticatedUserId, LogoutRequest request) {
        if (authenticatedUserId == null
                || request == null
                || request.getRefreshToken() == null
                || request.getRefreshToken().isBlank()) {
            throw AuthExceptions.invalidRefreshToken();
        }

        ParsedToken parsedToken = tokenParserPort.parseToken(request.getRefreshToken(), TokenType.REFRESH);
        if (!authenticatedUserId.equals(parsedToken.getUserId())) {
            throw AuthExceptions.invalidRefreshToken();
        }
        String tokenHash = tokenHashPort.sha256(request.getRefreshToken());
        if (!blacklistPort.existsByTokenHash(tokenHash)) {
            blacklistPort.save(tokenHash, parsedToken.getUserId(), parsedToken.getExpiresAt());
        }
    }
}

