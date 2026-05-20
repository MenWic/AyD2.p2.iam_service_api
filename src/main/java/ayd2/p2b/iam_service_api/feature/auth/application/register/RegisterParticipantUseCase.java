package ayd2.p2b.iam_service_api.feature.auth.application.register;

import ayd2.p2b.iam_service_api.common.util.TextNormalizer;
import ayd2.p2b.iam_service_api.common.validation.PasswordRules;
import ayd2.p2b.iam_service_api.common.validation.PersonalIdValidator;
import ayd2.p2b.iam_service_api.feature.auth.application.exception.AuthExceptions;
import ayd2.p2b.iam_service_api.feature.auth.application.port.PasswordHasherPort;
import ayd2.p2b.iam_service_api.feature.auth.dto.response.AuthResponse;
import ayd2.p2b.iam_service_api.feature.user.dto.request.RegisterUserRequest;
import ayd2.p2b.iam_service_api.feature.user.mapper.UserMapper;
import ayd2.p2b.iam_service_api.feature.auth.application.port.TokenIssuerPort;
import ayd2.p2b.iam_service_api.feature.user.application.port.UserRepositoryPort;
import ayd2.p2b.iam_service_api.feature.user.application.exception.UserExceptions;
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
public class RegisterParticipantUseCase {

    private final UserRepositoryPort userRepository;
    private final UserMapper userMapper;
    private final PasswordHasherPort passwordHasher;
    private final TokenIssuerPort tokenIssuerPort;

    public RegisterParticipantUseCase(
            UserRepositoryPort userRepository,
            UserMapper userMapper,
            PasswordHasherPort passwordHasher,
            TokenIssuerPort tokenIssuerPort
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordHasher = passwordHasher;
        this.tokenIssuerPort = tokenIssuerPort;
    }

    @Transactional
    public AuthResponse execute(RegisterUserRequest request) {
        if (request == null) {
            throw AuthExceptions.validationFailed("Register request is required");
        }

        String normalizedEmail = TextNormalizer.normalizeEmail(request.getEmail());
        String normalizedPassword = TextNormalizer.trimRequired(request.getPassword());
        String personalId = TextNormalizer.trimRequired(request.getPersonalId());

        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            throw AuthExceptions.validationFailed("Email is required");
        }

        PersonalIdValidator.validateAlphanumeric(personalId);
        PasswordRules.validateRequiredPassword(normalizedPassword);

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw UserExceptions.emailAlreadyRegistered();
        }
        if (userRepository.existsByPersonalIdIgnoreCase(personalId)) {
            throw UserExceptions.personalIdAlreadyRegistered();
        }

        UserAccount saved = userRepository.save(UserAccount.builder()
                .email(normalizedEmail)
                .passwordHash(passwordHasher.encode(normalizedPassword))
                .fullName(TextNormalizer.trimRequired(request.getFullName()))
                .organization(TextNormalizer.trimRequired(request.getOrganization()))
                .phone(TextNormalizer.trimRequired(request.getPhone()))
                .personalId(personalId)
                .photoUrl(TextNormalizer.trimToNull(request.getPhotoUrl()))
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

