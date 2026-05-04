package ayd2.p2b.iam_service_api.application.usecase.user;

import ayd2.p2b.iam_service_api.application.dto.user.UserResponse;
import ayd2.p2b.iam_service_api.application.mapper.user.UserMapper;
import ayd2.p2b.iam_service_api.application.port.user.UserRepositoryPort;
import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.domain.model.user.UserAccount;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class GetCurrentUserUseCase {

    private final UserRepositoryPort userRepository;
    private final UserMapper userMapper;

    public GetCurrentUserUseCase(UserRepositoryPort userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public UserResponse execute(UUID userId) {
        UserAccount user = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "resource.not_found", "User not found"));
        return userMapper.toResponse(user);
    }
}
