package ayd2.p2b.iam_service_api.feature.auth.application.login;

import ayd2.p2b.iam_service_api.feature.auth.dto.response.AuthResponse;
import ayd2.p2b.iam_service_api.feature.auth.dto.request.LoginRequest;
import ayd2.p2b.iam_service_api.feature.user.mapper.UserMapper;
import ayd2.p2b.iam_service_api.feature.auth.application.port.TokenIssuerPort;
import ayd2.p2b.iam_service_api.feature.user.application.port.UserRepositoryPort;
import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LoginUseCase {

    private final UserRepositoryPort userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuerPort tokenIssuerPort;

    public LoginUseCase(
            UserRepositoryPort userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            TokenIssuerPort tokenIssuerPort
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenIssuerPort = tokenIssuerPort;
    }

    @Transactional(readOnly = true)
    public AuthResponse execute(LoginRequest request) {
        UserAccount user = userRepository.findByEmailIgnoreCase(request.getEmail().trim().toLowerCase())
                .orElseThrow(this::invalidCredentials);

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw invalidCredentials();
        }
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw invalidCredentials();
        }
        if (user.getRoles().isEmpty()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "domain.invariant_violated", "User has no roles");
        }

        return AuthResponse.builder()
                .accessToken(tokenIssuerPort.generateAccessToken(user))
                .refreshToken(tokenIssuerPort.generateRefreshToken(user))
                .user(userMapper.toResponse(user))
                .build();
    }

    private ApiException invalidCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "auth.invalid_credentials", "Invalid credentials");
    }
}

