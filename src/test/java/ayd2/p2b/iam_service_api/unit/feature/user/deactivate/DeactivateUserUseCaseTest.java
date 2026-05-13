package ayd2.p2b.iam_service_api.unit.feature.user.deactivate;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.feature.user.application.deactivate.DeactivateUserUseCase;
import ayd2.p2b.iam_service_api.feature.user.application.port.UserRepositoryPort;
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import ayd2.p2b.iam_service_api.feature.user.dto.internal.RequesterContext;
import ayd2.p2b.iam_service_api.feature.user.dto.response.UserResponse;
import ayd2.p2b.iam_service_api.feature.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeactivateUserUseCaseTest {

    @Mock private UserRepositoryPort userRepository;
    @Mock private UserMapper userMapper;

    private DeactivateUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeactivateUserUseCase(userRepository, userMapper);
    }

    @Test
    void should_deactivate_active_user_when_requester_is_system_admin() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        RequesterContext requester = new RequesterContext(requesterId, Set.of(Role.SYSTEM_ADMIN));
        UserAccount activeUser = user(targetId, true, Set.of(Role.PARTICIPANT));
        UserAccount savedUser = activeUser.toBuilder().active(false).updatedBy(requesterId).build();
        UserResponse response = response(targetId);

        when(userRepository.findById(targetId)).thenReturn(Optional.of(activeUser));
        when(userRepository.save(any(UserAccount.class))).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(response);

        UserResponse result = useCase.execute(requester, targetId);

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userRepository).save(captor.capture());
        assertEquals(Boolean.FALSE, captor.getValue().getActive());
        assertEquals(requesterId, captor.getValue().getUpdatedBy());
        assertEquals(targetId, result.getId());
    }

    @Test
    void should_be_idempotent_when_user_is_already_inactive() {
        UUID targetId = UUID.randomUUID();
        RequesterContext requester = new RequesterContext(UUID.randomUUID(), Set.of(Role.SYSTEM_ADMIN));
        UserAccount inactiveUser = user(targetId, false, Set.of(Role.PARTICIPANT));
        UserResponse response = response(targetId);

        when(userRepository.findById(targetId)).thenReturn(Optional.of(inactiveUser));
        when(userMapper.toResponse(inactiveUser)).thenReturn(response);

        UserResponse result = useCase.execute(requester, targetId);

        assertEquals(targetId, result.getId());
        verify(userRepository, never()).save(any(UserAccount.class));
    }

    @Test
    void should_throw_when_deactivating_last_active_system_admin() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        RequesterContext requester = new RequesterContext(requesterId, Set.of(Role.SYSTEM_ADMIN));
        UserAccount adminUser = user(targetId, true, Set.of(Role.SYSTEM_ADMIN, Role.PARTICIPANT));

        when(userRepository.findById(targetId)).thenReturn(Optional.of(adminUser));
        when(userRepository.countActiveByRole(Role.SYSTEM_ADMIN)).thenReturn(1L);

        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(requester, targetId));

        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, ex.getStatus());
        assertEquals("domain.invariant_violated", ex.getCode());
        verify(userRepository, never()).save(any(UserAccount.class));
    }

    @Test
    void should_throw_forbidden_when_requester_is_not_system_admin() {
        RequesterContext requester = new RequesterContext(UUID.randomUUID(), Set.of(Role.PARTICIPANT));

        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(requester, UUID.randomUUID()));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("auth.forbidden", ex.getCode());
        verify(userRepository, never()).findById(any(UUID.class));
    }

    @Test
    void should_throw_forbidden_when_requester_is_null() {
        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(null, UUID.randomUUID()));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("auth.forbidden", ex.getCode());
        verify(userRepository, never()).findById(any(UUID.class));
    }

    @Test
    void should_throw_validation_failed_when_target_user_id_is_null() {
        ApiException ex = assertThrows(
                ApiException.class,
                () -> useCase.execute(new RequesterContext(UUID.randomUUID(), Set.of(Role.SYSTEM_ADMIN)), null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("validation.failed", ex.getCode());
        verify(userRepository, never()).findById(any(UUID.class));
    }

    @Test
    void should_throw_not_found_when_user_does_not_exist() {
        RequesterContext requester = new RequesterContext(UUID.randomUUID(), Set.of(Role.SYSTEM_ADMIN));
        UUID targetId = UUID.randomUUID();

        when(userRepository.findById(targetId)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(requester, targetId));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("resource.not_found", ex.getCode());
    }

    private UserAccount user(UUID id, boolean active, Set<Role> roles) {
        return UserAccount.builder()
                .id(id)
                .email(id + "@domain.com")
                .active(active)
                .roles(roles)
                .build();
    }

    private UserResponse response(UUID id) {
        return UserResponse.builder()
                .id(id)
                .email(id + "@domain.com")
                .roles(Set.of("PARTICIPANT"))
                .build();
    }
}
