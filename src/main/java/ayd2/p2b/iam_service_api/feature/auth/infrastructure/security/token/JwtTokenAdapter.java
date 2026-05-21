package ayd2.p2b.iam_service_api.feature.auth.infrastructure.security.token;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.feature.auth.application.port.ParsedToken;
import ayd2.p2b.iam_service_api.feature.auth.application.port.TokenIssuerPort;
import ayd2.p2b.iam_service_api.feature.auth.application.port.TokenParserPort;
import ayd2.p2b.iam_service_api.feature.auth.domain.model.TokenType;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenAdapter implements TokenIssuerPort, TokenParserPort {

    private final JwtProperties properties;

    @Override
    public String generateAccessToken(UserAccount userAccount) {
        Instant now = Instant.now();
        Instant expiration = now.plus(properties.getExpirationMinutes(), ChronoUnit.MINUTES);
        List<String> roles = userAccount.getRoles() == null ? List.of()
                : userAccount.getRoles().stream().map(Enum::name).toList();

        return Jwts.builder()
                .subject(userAccount.getId().toString())
                .claims(Map.of(
                        "userId", userAccount.getId().toString(),
                        "email", userAccount.getEmail(),
                        "fullName", userAccount.getFullName() != null ? userAccount.getFullName() : "",
                        "roles", roles,
                        "tokenType", TokenType.ACCESS.name()))
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
                        "tokenType", TokenType.REFRESH.name()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(signingKey())
                .compact();
    }

    // JJWT's Claims#get returns raw List when the claim type is a JSON array;
    // the JWT spec guarantees roles is a List<String> since we wrote it that way in
    // generateAccessToken.
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
            String fullName = claims.get("fullName", String.class);
            Instant expiresAt = claims.getExpiration().toInstant();

            return ParsedToken.builder()
                    .userId(userId)
                    .subject(claims.getSubject())
                    .email(claims.get("email", String.class))
                    .fullName(fullName)
                    .roles(roles == null ? List.of() : roles)
                    .tokenType(TokenType.valueOf(tokenTypeClaim))
                    .expiresAt(expiresAt)
                    .build();
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
