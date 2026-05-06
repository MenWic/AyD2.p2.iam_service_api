package ayd2.p2b.iam_service_api.unit.feature.user.current;

import ayd2.p2b.iam_service_api.feature.user.application.current.GetCurrentUserUseCase;
import ayd2.p2b.iam_service_api.feature.user.application.port.UserRepositoryPort;
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import ayd2.p2b.iam_service_api.feature.user.dto.response.UserResponse;
import ayd2.p2b.iam_service_api.feature.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCurrentUserUseCaseTest {

    @Mock private UserRepositoryPort userRepository;
    @Mock private UserMapper userMapper;

    private GetCurrentUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetCurrentUserUseCase(userRepository, userMapper);
    }

    @Test
    void should_return_user_profile_when_user_exists_and_active() {
        UUID userId = UUID.randomUUID();
        UserAccount entity = UserAccount.builder().id(userId).email("participant@domain.com").active(true).roles(Set.of(Role.PARTICIPANT)).build();
        UserResponse response = UserResponse.builder().id(userId).email("participant@domain.com").roles(Set.of("PARTICIPANT")).build();

        when(userRepository.findByIdAndActiveTrue(userId)).thenReturn(Optional.of(entity));
        when(userMapper.toResponse(entity)).thenReturn(response);

        UserResponse result = useCase.execute(userId);

        assertEquals(userId, result.getId());
    }
}

