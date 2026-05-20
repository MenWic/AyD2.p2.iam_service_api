package ayd2.p2b.iam_service_api.feature.user.application.create_system_admin;

import ayd2.p2b.iam_service_api.common.util.TextNormalizer;
import ayd2.p2b.iam_service_api.common.validation.PasswordRules;
import ayd2.p2b.iam_service_api.common.validation.PersonalIdValidator;
import ayd2.p2b.iam_service_api.feature.user.application.exception.UserExceptions;
import ayd2.p2b.iam_service_api.feature.auth.application.port.PasswordHasherPort;
import ayd2.p2b.iam_service_api.feature.user.application.port.UserRepositoryPort;
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import ayd2.p2b.iam_service_api.feature.user.dto.internal.RequesterContext;
import ayd2.p2b.iam_service_api.feature.user.dto.request.CreateSystemAdminRequest;
import ayd2.p2b.iam_service_api.feature.user.dto.response.UserResponse;
import ayd2.p2b.iam_service_api.feature.user.mapper.UserMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
public class CreateSystemAdminUseCase {

    private final UserRepositoryPort userRepository;
    private final UserMapper userMapper;
    private final PasswordHasherPort passwordHasher;

    public CreateSystemAdminUseCase(
            UserRepositoryPort userRepository,
            UserMapper userMapper,
            PasswordHasherPort passwordHasher
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public UserResponse execute(RequesterContext requester, CreateSystemAdminRequest request) {
        requireRole(requester, Role.SYSTEM_ADMIN);
        if (request == null) {
            throw UserExceptions.validationFailed("Create system admin request is required");
        }

        String email = TextNormalizer.normalizeEmail(request.getEmail());
        String personalId = TextNormalizer.trimRequired(request.getPersonalId());
        String password = TextNormalizer.trimRequired(request.getPassword());
        if (email == null || email.isBlank()) {
            throw UserExceptions.validationFailed("Email is required");
        }
        PersonalIdValidator.validateAlphanumeric(personalId);
        PasswordRules.validateRequiredPassword(password);

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw UserExceptions.emailAlreadyRegistered();
        }
        if (userRepository.existsByPersonalIdIgnoreCase(personalId)) {
            throw UserExceptions.personalIdAlreadyRegistered();
        }

        UserAccount saved = userRepository.save(UserAccount.builder()
                .email(email)
                .passwordHash(passwordHasher.encode(password))
                .fullName(TextNormalizer.trimRequired(request.getFullName()))
                .organization(TextNormalizer.trimRequired(request.getOrganization()))
                .phone(TextNormalizer.trimRequired(request.getPhone()))
                .personalId(personalId)
                .photoUrl(TextNormalizer.trimToNull(request.getPhotoUrl()))
                .active(Boolean.TRUE)
                .roles(Set.of(Role.SYSTEM_ADMIN, Role.PARTICIPANT))
                .linkedInstitutions(Set.of())
                .createdBy(requester.getUserId())
                .updatedBy(requester.getUserId())
                .build());

        return userMapper.toResponse(saved);
    }

    private void requireRole(RequesterContext requester, Role role) {
        if (requester == null
                || requester.getUserId() == null
                || requester.getRoles() == null
                || !requester.getRoles().contains(role)) {
            throw UserExceptions.forbidden();
        }
    }
}
