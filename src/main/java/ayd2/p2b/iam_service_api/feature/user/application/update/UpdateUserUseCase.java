package ayd2.p2b.iam_service_api.feature.user.application.update;

import ayd2.p2b.iam_service_api.common.util.TextNormalizer;
import ayd2.p2b.iam_service_api.feature.user.application.exception.UserExceptions;
import ayd2.p2b.iam_service_api.feature.user.application.port.UserRepositoryPort;
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import ayd2.p2b.iam_service_api.feature.user.dto.internal.RequesterContext;
import ayd2.p2b.iam_service_api.feature.user.dto.request.UpdateUserRequest;
import ayd2.p2b.iam_service_api.feature.user.dto.response.UserResponse;
import ayd2.p2b.iam_service_api.feature.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpdateUserUseCase {

    private final UserRepositoryPort userRepository;
    private final UserMapper userMapper;


    @Transactional
    public UserResponse execute(RequesterContext requester, UUID targetUserId, UpdateUserRequest request) {
        if (targetUserId == null) {
            throw UserExceptions.validationFailed("targetUserId is required");
        }
        if (request == null) {
            throw UserExceptions.validationFailed("Update request is required");
        }
        if (!canUpdate(requester, targetUserId)) {
            throw UserExceptions.forbidden();
        }

        UserAccount targetUser = userRepository.findById(targetUserId)
                .orElseThrow(UserExceptions::notFound);

        UserAccount updatedUser = targetUser.toBuilder()
                .fullName(TextNormalizer.trimRequired(request.getFullName()))
                .organization(TextNormalizer.trimRequired(request.getOrganization()))
                .phone(TextNormalizer.trimRequired(request.getPhone()))
                .photoUrl(TextNormalizer.trimToNull(request.getPhotoUrl()))
                .updatedBy(requester.getUserId())
                .build();

        return userMapper.toResponse(userRepository.save(updatedUser));
    }

    private boolean canUpdate(RequesterContext requester, UUID targetUserId) {
        if (requester == null || requester.getUserId() == null) {
            return false;
        }

        boolean isSystemAdmin = requester.getRoles() != null
                && requester.getRoles().contains(Role.SYSTEM_ADMIN);
        boolean isSelf = requester.getUserId() != null && requester.getUserId().equals(targetUserId);
        return isSystemAdmin || isSelf;
    }
}
