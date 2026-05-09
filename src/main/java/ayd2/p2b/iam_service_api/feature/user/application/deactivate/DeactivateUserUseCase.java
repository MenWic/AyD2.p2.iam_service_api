package ayd2.p2b.iam_service_api.feature.user.application.deactivate;

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

import java.util.UUID;

@Component
public class DeactivateUserUseCase {

    private final UserRepositoryPort userRepository;
    private final UserMapper userMapper;

    public DeactivateUserUseCase(UserRepositoryPort userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResponse execute(RequesterContext requester, UUID targetUserId) {
        requireSystemAdmin(requester);

        UserAccount targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "resource.not_found", "User not found"));

        if (!Boolean.TRUE.equals(targetUser.getActive())) {
            return userMapper.toResponse(targetUser);
        }

        if (targetUser.getRoles() != null
                && targetUser.getRoles().contains(Role.SYSTEM_ADMIN)
                && userRepository.countActiveByRole(Role.SYSTEM_ADMIN) == 1) {
            throw new ApiException(
                    HttpStatus.UNPROCESSABLE_CONTENT,
                    "domain.invariant_violated",
                    "At least one active system admin must exist"
            );
        }

        UserAccount updated = targetUser.toBuilder()
                .active(Boolean.FALSE)
                .updatedBy(requester.getUserId())
                .build();

        return userMapper.toResponse(userRepository.save(updated));
    }

    private void requireSystemAdmin(RequesterContext requester) {
        if (requester.getRoles() == null || !requester.getRoles().contains(Role.SYSTEM_ADMIN)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "auth.forbidden", "Forbidden");
        }
    }
}
