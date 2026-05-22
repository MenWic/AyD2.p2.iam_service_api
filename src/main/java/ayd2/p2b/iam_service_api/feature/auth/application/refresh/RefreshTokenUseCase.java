package ayd2.p2b.iam_service_api.feature.auth.application.refresh;

import ayd2.p2b.iam_service_api.feature.auth.application.exception.AuthExceptions;
import ayd2.p2b.iam_service_api.feature.auth.dto.request.RefreshRequest;
import ayd2.p2b.iam_service_api.feature.auth.dto.response.RefreshResponse;
import ayd2.p2b.iam_service_api.feature.auth.application.port.RefreshTokenBlacklistPort;
import ayd2.p2b.iam_service_api.feature.auth.application.port.ParsedToken;
import ayd2.p2b.iam_service_api.feature.auth.application.port.TokenHashPort;
import ayd2.p2b.iam_service_api.feature.auth.application.port.TokenIssuerPort;
import ayd2.p2b.iam_service_api.feature.auth.application.port.TokenParserPort;
import ayd2.p2b.iam_service_api.feature.user.application.port.UserRepositoryPort;
import ayd2.p2b.iam_service_api.feature.auth.domain.model.TokenType;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RefreshTokenUseCase {

    private final UserRepositoryPort userRepository;
    private final RefreshTokenBlacklistPort blacklistPort;
    private final TokenIssuerPort tokenIssuerPort;
    private final TokenParserPort tokenParserPort;
    private final TokenHashPort tokenHashPort;


    @Transactional(readOnly = true)
    public RefreshResponse execute(RefreshRequest request) {
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            throw AuthExceptions.invalidRefreshToken();
        }

        String refreshToken = request.getRefreshToken();
        ParsedToken parsedToken = tokenParserPort.parseToken(refreshToken, TokenType.REFRESH);
        String tokenHash = tokenHashPort.sha256(refreshToken);
        if (blacklistPort.existsByTokenHash(tokenHash)) {
            throw AuthExceptions.blacklistedRefreshToken();
        }
        UserAccount user = userRepository.findByIdAndActiveTrue(parsedToken.getUserId())
                .orElseThrow(AuthExceptions::invalidRefreshToken);

        return RefreshResponse.builder()
                .accessToken(tokenIssuerPort.generateAccessToken(user))
                .build();
    }
}

