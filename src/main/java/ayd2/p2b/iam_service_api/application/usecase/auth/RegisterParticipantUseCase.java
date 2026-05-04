package ayd2.p2b.iam_service_api.application.usecase.auth;

import ayd2.p2b.iam_service_api.application.dto.auth.AuthResponse;
import ayd2.p2b.iam_service_api.application.dto.user.RegisterUserRequest;
import ayd2.p2b.iam_service_api.application.mapper.user.UserMapper;
import ayd2.p2b.iam_service_api.application.port.security.TokenIssuerPort;
import ayd2.p2b.iam_service_api.application.port.user.UserRepositoryPort;
import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.domain.model.user.Role;
import ayd2.p2b.iam_service_api.domain.model.user.UserAccount;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
public class RegisterParticipantUseCase {

    private final UserRepositoryPort userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuerPort tokenIssuerPort;

    public RegisterParticipantUseCase(
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

    @Transactional
    public AuthResponse execute(RegisterUserRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ApiException(HttpStatus.CONFLICT, "resource.conflict", "Email already registered");
        }

        UserAccount saved = userRepository.save(UserAccount.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .organization(request.getOrganization())
                .phone(request.getPhone())
                .personalId(request.getPersonalId())
                .photoUrl(request.getPhotoUrl())
                .active(Boolean.TRUE)
                .roles(Set.of(Role.PARTICIPANT))
                .build());

        return AuthResponse.builder()
                .accessToken(tokenIssuerPort.generateAccessToken(saved))
                .refreshToken(tokenIssuerPort.generateRefreshToken(saved))
                .user(userMapper.toResponse(saved))
                .build();
    }
}
