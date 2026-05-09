package ayd2.p2b.iam_service_api.feature.auth.application.refresh;

import ayd2.p2b.iam_service_api.feature.auth.dto.request.RefreshRequest;
import ayd2.p2b.iam_service_api.feature.auth.dto.response.RefreshResponse;
import ayd2.p2b.iam_service_api.feature.auth.application.port.RefreshTokenBlacklistPort;
import ayd2.p2b.iam_service_api.feature.auth.application.port.ParsedToken;
import ayd2.p2b.iam_service_api.feature.auth.application.port.TokenHashPort;
import ayd2.p2b.iam_service_api.feature.auth.application.port.TokenIssuerPort;
import ayd2.p2b.iam_service_api.feature.auth.application.port.TokenParserPort;
import ayd2.p2b.iam_service_api.feature.user.application.port.UserRepositoryPort;
import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.feature.auth.domain.model.TokenType;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
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
        UserAccount user = userRepository.findByIdAndActiveTrue(parsedToken.getUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "auth.token_invalid", "User not active"));

        return RefreshResponse.builder()
                .accessToken(tokenIssuerPort.generateAccessToken(user))
                .build();
    }
}

