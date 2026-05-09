package ayd2.p2b.iam_service_api.feature.user.application.get;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.feature.user.application.port.UserRepositoryPort;
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import ayd2.p2b.iam_service_api.feature.user.dto.internal.RequesterContext;
import ayd2.p2b.iam_service_api.feature.user.dto.response.UserResponse;
import ayd2.p2b.iam_service_api.feature.user.mapper.UserMapper;
import org.springframework.http.HttpStatus;
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
        UserAccount targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "resource.not_found", "User not found"));

        if (hasRole(requester, Role.SYSTEM_ADMIN) || requester.getUserId().equals(targetUserId)) {
            return userMapper.toResponse(targetUser);
        }

        if (!hasRole(requester, Role.CONGRESS_ADMIN)) {
            throw forbidden();
        }

        UserAccount requesterAccount = userRepository.findById(requester.getUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "resource.not_found", "User not found"));

        Set<UUID> requesterInstitutions = requesterAccount.getLinkedInstitutions() == null
                ? Set.of()
                : requesterAccount.getLinkedInstitutions();
        Set<UUID> targetInstitutions = targetUser.getLinkedInstitutions() == null
                ? Set.of()
                : targetUser.getLinkedInstitutions();

        if (targetInstitutions.isEmpty()) {
            throw forbidden();
        }

        boolean hasSharedInstitution = requesterInstitutions.stream().anyMatch(targetInstitutions::contains);
        if (!hasSharedInstitution) {
            throw forbidden();
        }

        return userMapper.toResponse(targetUser);
    }

    private boolean hasRole(RequesterContext requester, Role role) {
        return requester.getRoles() != null && requester.getRoles().contains(role);
    }

    private ApiException forbidden() {
        return new ApiException(HttpStatus.FORBIDDEN, "auth.forbidden", "Forbidden");
    }
}
