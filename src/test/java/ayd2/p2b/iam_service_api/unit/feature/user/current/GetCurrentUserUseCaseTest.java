package ayd2.p2b.iam_service_api.unit.feature.user.current;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
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
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        UserAccount entity = UserAccount.builder()
                .id(userId)
                .email("participant@domain.com")
                .active(true)
                .roles(Set.of(Role.PARTICIPANT))
                .build();
        UserResponse response = UserResponse.builder()
                .id(userId)
                .email("participant@domain.com")
                .roles(Set.of("PARTICIPANT"))
                .build();

        when(userRepository.findByIdAndActiveTrue(userId)).thenReturn(Optional.of(entity));
        when(userMapper.toResponse(entity)).thenReturn(response);

        UserResponse result = useCase.execute(userId);

        assertEquals(userId, result.getId());
    }

    @Test
    void should_throw_validation_failed_when_user_id_is_null() {
        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("validation.failed", ex.getCode());
    }

    @Test
    void should_throw_not_found_when_user_does_not_exist_or_is_inactive() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findByIdAndActiveTrue(userId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> useCase.execute(userId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("resource.not_found", exception.getCode());
    }
}
