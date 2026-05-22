package ayd2.p2b.iam_service_api.feature.user.application.current;

import ayd2.p2b.iam_service_api.feature.user.application.exception.UserExceptions;
import ayd2.p2b.iam_service_api.feature.user.dto.response.UserResponse;
import ayd2.p2b.iam_service_api.feature.user.mapper.UserMapper;
import ayd2.p2b.iam_service_api.feature.user.application.port.UserRepositoryPort;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetCurrentUserUseCase {

    private final UserRepositoryPort userRepository;
    private final UserMapper userMapper;


    @Transactional(readOnly = true)
    public UserResponse execute(UUID userId) {
        if (userId == null) {
            throw UserExceptions.validationFailed("userId is required");
        }
        UserAccount user = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(UserExceptions::notFound);
        return userMapper.toResponse(user);
    }
}

