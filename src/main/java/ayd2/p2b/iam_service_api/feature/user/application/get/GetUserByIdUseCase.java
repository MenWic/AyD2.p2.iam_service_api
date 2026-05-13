package ayd2.p2b.iam_service_api.feature.user.application.get;

import ayd2.p2b.iam_service_api.feature.user.application.exception.UserExceptions;
import ayd2.p2b.iam_service_api.feature.user.application.port.UserRepositoryPort;
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import ayd2.p2b.iam_service_api.feature.user.dto.internal.RequesterContext;
import ayd2.p2b.iam_service_api.feature.user.dto.response.UserResponse;
import ayd2.p2b.iam_service_api.feature.user.mapper.UserMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Component
public class GetUserByIdUseCase {

    private final UserRepositoryPort userRepository;
    private final UserMapper userMapper;

    public GetUserByIdUseCase(UserRepositoryPort userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public UserResponse execute(RequesterContext requester, UUID targetUserId) {
        if (targetUserId == null) {
            throw UserExceptions.validationFailed("targetUserId is required");
        }
        requireValidRequester(requester);

        if (requester.getUserId().equals(targetUserId) || hasRole(requester, Role.SYSTEM_ADMIN)) {
            UserAccount targetUser = userRepository.findById(targetUserId)
                    .orElseThrow(UserExceptions::notFound);
            return userMapper.toResponse(targetUser);
        }

        if (!hasRole(requester, Role.CONGRESS_ADMIN)) {
            throw UserExceptions.forbidden();
        }

        UserAccount targetUser = userRepository.findById(targetUserId)
                .orElseThrow(UserExceptions::notFound);

        UserAccount requesterAccount = userRepository.findById(requester.getUserId())
                .orElseThrow(UserExceptions::notFound);

        Set<UUID> requesterInstitutions = requesterAccount.getLinkedInstitutions() == null
                ? Set.of()
                : requesterAccount.getLinkedInstitutions();
        Set<UUID> targetInstitutions = targetUser.getLinkedInstitutions() == null
                ? Set.of()
                : targetUser.getLinkedInstitutions();

        if (targetInstitutions.isEmpty()) {
            throw UserExceptions.forbidden();
        }

        boolean hasSharedInstitution = requesterInstitutions.stream().anyMatch(targetInstitutions::contains);
        if (!hasSharedInstitution) {
            throw UserExceptions.forbidden();
        }

        return userMapper.toResponse(targetUser);
    }

    private boolean hasRole(RequesterContext requester, Role role) {
        return requester != null && requester.getRoles() != null && requester.getRoles().contains(role);
    }

    private void requireValidRequester(RequesterContext requester) {
        if (requester == null || requester.getUserId() == null || requester.getRoles() == null || requester.getRoles().isEmpty()) {
            throw UserExceptions.forbidden();
        }
    }
}
