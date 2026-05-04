package ayd2.p2b.iam_service_api.application.usecase.auth;

import ayd2.p2b.iam_service_api.application.dto.auth.RefreshRequest;
import ayd2.p2b.iam_service_api.application.dto.auth.RefreshResponse;
import ayd2.p2b.iam_service_api.application.port.auth.RefreshTokenBlacklistPort;
import ayd2.p2b.iam_service_api.application.port.security.ParsedToken;
import ayd2.p2b.iam_service_api.application.port.security.TokenHashPort;
import ayd2.p2b.iam_service_api.application.port.security.TokenIssuerPort;
import ayd2.p2b.iam_service_api.application.port.security.TokenParserPort;
import ayd2.p2b.iam_service_api.application.port.user.UserRepositoryPort;
import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.domain.model.auth.TokenType;
import ayd2.p2b.iam_service_api.domain.model.user.UserAccount;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RefreshTokenUseCase {

    private final UserRepositoryPort userRepository;
    private final RefreshTokenBlacklistPort blacklistPort;
    private final TokenIssuerPort tokenIssuerPort;
    private final TokenParserPort tokenParserPort;
    private final TokenHashPort tokenHashPort;

    public RefreshTokenUseCase(
            UserRepositoryPort userRepository,
            RefreshTokenBlacklistPort blacklistPort,
            TokenIssuerPort tokenIssuerPort,
            TokenParserPort tokenParserPort,
            TokenHashPort tokenHashPort
    ) {
        this.userRepository = userRepository;
        this.blacklistPort = blacklistPort;
        this.tokenIssuerPort = tokenIssuerPort;
        this.tokenParserPort = tokenParserPort;
        this.tokenHashPort = tokenHashPort;
    }

    @Transactional(readOnly = true)
    public RefreshResponse execute(RefreshRequest request) {
        ParsedToken parsedToken = tokenParserPort.parseToken(request.getRefreshToken(), TokenType.REFRESH);
        String tokenHash = tokenHashPort.sha256(request.getRefreshToken());
        if (blacklistPort.existsByTokenHash(tokenHash)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "auth.token_invalid", "Refresh token is invalidated");
        }
        UserAccount user = userRepository.findByIdAndActiveTrue(parsedToken.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "auth.token_invalid", "User not active"));

        return RefreshResponse.builder()
                .accessToken(tokenIssuerPort.generateAccessToken(user))
                .build();
    }
}
