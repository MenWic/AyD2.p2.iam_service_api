package ayd2.p2b.iam_service_api.feature.user.application.current;

import ayd2.p2b.iam_service_api.feature.user.dto.response.UserResponse;
import ayd2.p2b.iam_service_api.feature.user.mapper.UserMapper;
import ayd2.p2b.iam_service_api.feature.user.application.port.UserRepositoryPort;
import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
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

