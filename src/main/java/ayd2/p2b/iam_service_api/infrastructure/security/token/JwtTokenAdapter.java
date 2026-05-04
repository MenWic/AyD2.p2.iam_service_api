package ayd2.p2b.iam_service_api.infrastructure.security.token;

import ayd2.p2b.iam_service_api.application.port.security.ParsedToken;
import ayd2.p2b.iam_service_api.application.port.security.TokenIssuerPort;
import ayd2.p2b.iam_service_api.application.port.security.TokenParserPort;
import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.domain.model.auth.TokenType;
import ayd2.p2b.iam_service_api.domain.model.user.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtTokenAdapter implements TokenIssuerPort, TokenParserPort {

    private final JwtProperties properties;

    public JwtTokenAdapter(JwtProperties properties) {
        this.properties = properties;
    }

    @Override
    public String generateAccessToken(UserAccount userAccount) {
        Instant now = Instant.now();
        Instant expiration = now.plus(properties.getExpirationMinutes(), ChronoUnit.MINUTES);
        List<String> roles = userAccount.getRoles() == null ? List.of() : userAccount.getRoles().stream().map(Enum::name).toList();

        return Jwts.builder()
                .subject(userAccount.getId().toString())
                .claims(Map.of(
                        "userId", userAccount.getId().toString(),
                        "email", userAccount.getEmail(),
                        "roles", roles,
                        "tokenType", TokenType.ACCESS.name()
                ))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(signingKey())
                .compact();
    }

    @Override
    public String generateRefreshToken(UserAccount userAccount) {
        Instant now = Instant.now();
        Instant expiration = now.plus(properties.getRefreshExpirationDays(), ChronoUnit.DAYS);

        return Jwts.builder()
                .subject(userAccount.getId().toString())
                .claims(Map.of(
                        "userId", userAccount.getId().toString(),
                        "tokenType", TokenType.REFRESH.name()
                ))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(signingKey())
                .compact();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ParsedToken parseToken(String token, TokenType expectedType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String tokenTypeClaim = claims.get("tokenType", String.class);
            if (!expectedType.name().equals(tokenTypeClaim)) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "auth.token_invalid", "Invalid token type");
            }

            UUID userId = UUID.fromString(claims.get("userId", String.class));
            List<String> roles = claims.get("roles", List.class);
            Instant expiresAt = claims.getExpiration().toInstant();

            return new ParsedToken(
                    userId,
                    claims.getSubject(),
                    claims.get("email", String.class),
                    roles == null ? List.of() : roles,
                    TokenType.valueOf(tokenTypeClaim),
                    expiresAt
            );
        } catch (ExpiredJwtException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "auth.token_expired", "Token expired");
        } catch (JwtException | IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "auth.token_invalid", "Invalid token");
        }
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
