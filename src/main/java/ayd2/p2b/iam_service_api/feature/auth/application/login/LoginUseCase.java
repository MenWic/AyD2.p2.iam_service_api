package ayd2.p2b.iam_service_api.feature.auth.application.login;

import ayd2.p2b.iam_service_api.common.util.TextNormalizer;
import ayd2.p2b.iam_service_api.feature.auth.dto.response.AuthResponse;
import ayd2.p2b.iam_service_api.feature.auth.dto.request.LoginRequest;
import ayd2.p2b.iam_service_api.feature.user.mapper.UserMapper;
import ayd2.p2b.iam_service_api.feature.auth.application.exception.AuthExceptions;
import ayd2.p2b.iam_service_api.core.security.password.PasswordHasherPort;
import ayd2.p2b.iam_service_api.feature.auth.application.port.TokenIssuerPort;
import ayd2.p2b.iam_service_api.feature.user.application.port.UserRepositoryPort;
import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LoginUseCase {

    private final UserRepositoryPort userRepository;
    private final UserMapper userMapper;
    private final PasswordHasherPort passwordHasher;
    private final TokenIssuerPort tokenIssuerPort;

    public LoginUseCase(
            UserRepositoryPort userRepository,
            UserMapper userMapper,
            PasswordHasherPort passwordHasher,
            TokenIssuerPort tokenIssuerPort) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordHasher = passwordHasher;
        this.tokenIssuerPort = tokenIssuerPort;
    }

    @Transactional(readOnly = true)
    public AuthResponse execute(LoginRequest request) {
        if (request == null) {
            throw AuthExceptions.validationFailed("Login request is required");
        }

        String normalizedEmail = TextNormalizer.normalizeEmail(request.getEmail());
        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            throw AuthExceptions.invalidCredentials();
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw AuthExceptions.invalidCredentials();
        }

        UserAccount user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(AuthExceptions::invalidCredentials);

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw AuthExceptions.invalidCredentials();
        }
        if (user.getPasswordHash() == null || !passwordHasher.matches(request.getPassword(), user.getPasswordHash())) {
            throw AuthExceptions.invalidCredentials();
        }
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "domain.invariant_violated", "User has no roles");
        }

        return AuthResponse.builder()
                .accessToken(tokenIssuerPort.generateAccessToken(user))
                .refreshToken(tokenIssuerPort.generateRefreshToken(user))
                .user(userMapper.toResponse(user))
                .build();
    }
}
