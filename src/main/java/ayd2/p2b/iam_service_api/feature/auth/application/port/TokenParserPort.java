package ayd2.p2b.iam_service_api.feature.auth.application.port;

import ayd2.p2b.iam_service_api.feature.auth.domain.model.TokenType;

public interface TokenParserPort {
    ParsedToken parseToken(String token, TokenType expectedType);
}

