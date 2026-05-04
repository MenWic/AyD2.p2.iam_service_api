package ayd2.p2b.iam_service_api.application.port.security;

import ayd2.p2b.iam_service_api.domain.model.user.UserAccount;

public interface TokenIssuerPort {

    String generateAccessToken(UserAccount userAccount);

    String generateRefreshToken(UserAccount userAccount);
}

