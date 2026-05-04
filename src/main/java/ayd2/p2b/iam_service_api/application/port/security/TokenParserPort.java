package ayd2.p2b.iam_service_api.application.port.security;

import ayd2.p2b.iam_service_api.domain.model.auth.TokenType;

public interface TokenParserPort {

    ParsedToken parseToken(String token, TokenType expectedType);
}

