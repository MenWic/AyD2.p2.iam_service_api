package ayd2.p2b.iam_service_api.feature.auth.application.port;

import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;

public interface TokenIssuerPort {
    String generateAccessToken(UserAccount userAccount);
    String generateRefreshToken(UserAccount userAccount);
}

