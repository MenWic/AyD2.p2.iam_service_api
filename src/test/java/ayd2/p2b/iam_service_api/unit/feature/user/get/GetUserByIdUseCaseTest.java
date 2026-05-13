package ayd2.p2b.iam_service_api.unit.feature.user.get;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.feature.user.application.get.GetUserByIdUseCase;
import ayd2.p2b.iam_service_api.feature.user.application.port.UserRepositoryPort;
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import ayd2.p2b.iam_service_api.feature.user.dto.internal.RequesterContext;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserByIdUseCaseTest {

    @Mock private UserRepositoryPort userRepository;
    @Mock private UserMapper userMapper;

    private GetUserByIdUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetUserByIdUseCase(userRepository, userMapper);
    }

    @Test
    void should_return_user_when_requester_is_system_admin() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        RequesterContext requester = new RequesterContext(requesterId, Set.of(Role.SYSTEM_ADMIN));
        UserAccount target = user(targetId, Set.of(Role.PARTICIPANT), Set.of());
        UserResponse response = response(targetId);

        when(userRepository.findById(targetId)).thenReturn(Optional.of(target));
        when(userMapper.toResponse(target)).thenReturn(response);

        UserResponse result = useCase.execute(requester, targetId);

        assertEquals(targetId, result.getId());
    }

    @Test
    void should_return_user_when_requester_is_self() {
        UUID userId = UUID.randomUUID();
        RequesterContext requester = new RequesterContext(userId, Set.of(Role.PARTICIPANT));
        UserAccount target = user(userId, Set.of(Role.PARTICIPANT), Set.of());
        UserResponse response = response(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(target));
        when(userMapper.toResponse(target)).thenReturn(response);

        UserResponse result = useCase.execute(requester, userId);

        assertEquals(userId, result.getId());
    }

    @Test
    void should_return_user_when_congress_admin_shares_institution() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UUID sharedInstitution = UUID.randomUUID();
        RequesterContext requester = new RequesterContext(requesterId, Set.of(Role.CONGRESS_ADMIN));
        UserAccount requesterAccount = user(requesterId, Set.of(Role.CONGRESS_ADMIN), Set.of(sharedInstitution));
        UserAccount target = user(targetId, Set.of(Role.PARTICIPANT), Set.of(sharedInstitution));
        UserResponse response = response(targetId);

        when(userRepository.findById(targetId)).thenReturn(Optional.of(target));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requesterAccount));
        when(userMapper.toResponse(target)).thenReturn(response);

        UserResponse result = useCase.execute(requester, targetId);

        assertEquals(targetId, result.getId());
    }

    @Test
    void should_throw_forbidden_when_congress_admin_does_not_share_institution() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        RequesterContext requester = new RequesterContext(requesterId, Set.of(Role.CONGRESS_ADMIN));
        UserAccount requesterAccount = user(requesterId, Set.of(Role.CONGRESS_ADMIN), Set.of(UUID.randomUUID()));
        UserAccount target = user(targetId, Set.of(Role.PARTICIPANT), Set.of(UUID.randomUUID()));

        when(userRepository.findById(targetId)).thenReturn(Optional.of(target));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requesterAccount));

        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(requester, targetId));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("auth.forbidden", ex.getCode());
    }

    @Test
    void should_throw_forbidden_when_requester_cannot_access_target() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        RequesterContext requester = new RequesterContext(requesterId, Set.of(Role.PARTICIPANT));

        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(requester, targetId));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("auth.forbidden", ex.getCode());
        verify(userRepository, never()).findById(targetId);
        verifyNoInteractions(userMapper);
    }

    @Test
    void should_throw_not_found_when_target_user_does_not_exist() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        RequesterContext requester = new RequesterContext(requesterId, Set.of(Role.SYSTEM_ADMIN));

        when(userRepository.findById(targetId)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(requester, targetId));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("resource.not_found", ex.getCode());
    }

    @Test
    void should_throw_validation_failed_when_target_user_id_is_null() {
        RequesterContext requester = new RequesterContext(UUID.randomUUID(), Set.of(Role.SYSTEM_ADMIN));

        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(requester, null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("validation.failed", ex.getCode());
    }

    @Test
    void should_throw_forbidden_when_requester_is_null() {
        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(null, UUID.randomUUID()));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("auth.forbidden", ex.getCode());
    }

    private UserAccount user(UUID id, Set<Role> roles, Set<UUID> institutions) {
        return UserAccount.builder()
                .id(id)
                .email(id + "@domain.com")
                .active(true)
                .roles(roles)
                .linkedInstitutions(institutions)
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
