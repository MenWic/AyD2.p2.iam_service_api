package ayd2.p2b.iam_service_api.unit.feature.user.update;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.feature.user.application.port.UserRepositoryPort;
import ayd2.p2b.iam_service_api.feature.user.application.update.UpdateUserUseCase;
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import ayd2.p2b.iam_service_api.feature.user.dto.internal.RequesterContext;
import ayd2.p2b.iam_service_api.feature.user.dto.request.UpdateUserRequest;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateUserUseCaseTest {

    @Mock private UserRepositoryPort userRepository;
    @Mock private UserMapper userMapper;

    private UpdateUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateUserUseCase(userRepository, userMapper);
    }

    @Test
    void should_update_own_profile_when_requester_is_self() {
        UUID userId = UUID.randomUUID();
        RequesterContext requester = requester(userId, Role.PARTICIPANT);
        UserAccount target = existingUser(userId);
        UpdateUserRequest request = validRequest();

        when(userRepository.findById(userId)).thenReturn(Optional.of(target));
        when(userRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponse(any(UserAccount.class))).thenReturn(response(userId));

        UserResponse result = useCase.execute(requester, userId, request);

        assertEquals(userId, result.getId());
        assertEquals("user@domain.com", result.getEmail());
    }

    @Test
    void should_update_any_profile_when_requester_is_system_admin() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        RequesterContext requester = requester(requesterId, Role.SYSTEM_ADMIN);
        UserAccount target = existingUser(targetId);
        UpdateUserRequest request = validRequest();

        when(userRepository.findById(targetId)).thenReturn(Optional.of(target));
        when(userRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponse(any(UserAccount.class))).thenReturn(response(targetId));

        UserResponse result = useCase.execute(requester, targetId, request);

        assertEquals(targetId, result.getId());
    }

    @Test
    void should_throw_forbidden_when_requester_updates_another_user() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        RequesterContext requester = requester(requesterId, Role.PARTICIPANT);

        ApiException ex = assertThrows(
                ApiException.class,
                () -> useCase.execute(requester, targetId, validRequest())
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("auth.forbidden", ex.getCode());
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any(UserAccount.class));
    }

    @Test
    void should_throw_forbidden_when_congress_admin_updates_another_user() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        RequesterContext requester = requester(requesterId, Role.CONGRESS_ADMIN);

        ApiException ex = assertThrows(
                ApiException.class,
                () -> useCase.execute(requester, targetId, validRequest())
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("auth.forbidden", ex.getCode());
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any(UserAccount.class));
    }

    @Test
    void should_throw_forbidden_when_requester_is_null_without_db_lookup() {
        UUID targetId = UUID.randomUUID();

        ApiException ex = assertThrows(
                ApiException.class,
                () -> useCase.execute(null, targetId, validRequest())
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("auth.forbidden", ex.getCode());
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any(UserAccount.class));
    }

    @Test
    void should_throw_forbidden_when_requester_user_id_is_null_without_db_lookup() {
        UUID targetId = UUID.randomUUID();
        RequesterContext requester = RequesterContext.builder()
                .userId(null)
                .roles(Set.of(Role.SYSTEM_ADMIN))
                .build();

        ApiException ex = assertThrows(
                ApiException.class,
                () -> useCase.execute(requester, targetId, validRequest())
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("auth.forbidden", ex.getCode());
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any(UserAccount.class));
    }

    @Test
    void should_throw_validation_failed_when_target_user_id_is_null_without_db_lookup() {
        ApiException ex = assertThrows(
                ApiException.class,
                () -> useCase.execute(requester(UUID.randomUUID(), Role.SYSTEM_ADMIN), null, validRequest())
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("validation.failed", ex.getCode());
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any(UserAccount.class));
    }

    @Test
    void should_throw_not_found_when_target_user_does_not_exist() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        when(userRepository.findById(targetId)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(
                ApiException.class,
                () -> useCase.execute(requester(requesterId, Role.SYSTEM_ADMIN), targetId, validRequest())
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("resource.not_found", ex.getCode());
    }

    @Test
    void should_preserve_roles() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UserAccount target = existingUser(targetId);

        UserAccount saved = executeAndCapture(
                requester(requesterId, Role.SYSTEM_ADMIN),
                targetId,
                target,
                validRequest()
        );

        assertEquals(target.getRoles(), saved.getRoles());
    }

    @Test
    void should_preserve_active() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UserAccount target = existingUser(targetId);

        UserAccount saved = executeAndCapture(
                requester(requesterId, Role.SYSTEM_ADMIN),
                targetId,
                target,
                validRequest()
        );

        assertEquals(target.getActive(), saved.getActive());
    }

    @Test
    void should_preserve_password_hash() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UserAccount target = existingUser(targetId);

        UserAccount saved = executeAndCapture(
                requester(requesterId, Role.SYSTEM_ADMIN),
                targetId,
                target,
                validRequest()
        );

        assertEquals(target.getPasswordHash(), saved.getPasswordHash());
    }

    @Test
    void should_preserve_email() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UserAccount target = existingUser(targetId);

        UserAccount saved = executeAndCapture(
                requester(requesterId, Role.SYSTEM_ADMIN),
                targetId,
                target,
                validRequest()
        );

        assertEquals(target.getEmail(), saved.getEmail());
    }

    @Test
    void should_preserve_personal_id() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UserAccount target = existingUser(targetId);

        UserAccount saved = executeAndCapture(
                requester(requesterId, Role.SYSTEM_ADMIN),
                targetId,
                target,
                validRequest()
        );

        assertEquals(target.getPersonalId(), saved.getPersonalId());
    }

    @Test
    void should_preserve_linked_institutions() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UserAccount target = existingUser(targetId);

        UserAccount saved = executeAndCapture(
                requester(requesterId, Role.SYSTEM_ADMIN),
                targetId,
                target,
                validRequest()
        );

        assertEquals(target.getLinkedInstitutions(), saved.getLinkedInstitutions());
    }

    @Test
    void should_preserve_created_by() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UserAccount target = existingUser(targetId);

        UserAccount saved = executeAndCapture(
                requester(requesterId, Role.SYSTEM_ADMIN),
                targetId,
                target,
                validRequest()
        );

        assertEquals(target.getCreatedBy(), saved.getCreatedBy());
    }

    @Test
    void should_set_updated_by_from_requester() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UserAccount target = existingUser(targetId);

        UserAccount saved = executeAndCapture(
                requester(requesterId, Role.SYSTEM_ADMIN),
                targetId,
                target,
                validRequest()
        );

        assertEquals(requesterId, saved.getUpdatedBy());
    }

    @Test
    void should_trim_profile_fields_before_save() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UserAccount target = existingUser(targetId);
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFullName("  Updated Name  ");
        request.setOrganization("  Updated Org  ");
        request.setPhone("  555-9999  ");
        request.setPhotoUrl("   ");

        UserAccount saved = executeAndCapture(
                requester(requesterId, Role.SYSTEM_ADMIN),
                targetId,
                target,
                request
        );

        assertEquals("Updated Name", saved.getFullName());
        assertEquals("Updated Org", saved.getOrganization());
        assertEquals("555-9999", saved.getPhone());
        assertNull(saved.getPhotoUrl());
    }

    private UserAccount executeAndCapture(
            RequesterContext requester,
            UUID targetId,
            UserAccount existing,
            UpdateUserRequest request
    ) {
        when(userRepository.findById(targetId)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponse(any(UserAccount.class))).thenReturn(response(targetId));

        useCase.execute(requester, targetId, request);

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userRepository).save(captor.capture());
        return captor.getValue();
    }

    private RequesterContext requester(UUID userId, Role role) {
        return RequesterContext.builder()
                .userId(userId)
                .roles(Set.of(role))
                .build();
    }

    private UpdateUserRequest validRequest() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFullName("Updated Name");
        request.setOrganization("Updated Org");
        request.setPhone("555-9999");
        request.setPhotoUrl("https://cdn.domain.com/new-photo.png");
        return request;
    }

    private UserAccount existingUser(UUID id) {
        return UserAccount.builder()
                .id(id)
                .email("user@domain.com")
                .passwordHash("hashed-password")
                .fullName("Original Name")
                .organization("Original Org")
                .phone("555-0101")
                .personalId("A123B")
                .photoUrl("https://cdn.domain.com/original.png")
                .active(Boolean.TRUE)
                .roles(Set.of(Role.CONGRESS_ADMIN, Role.PARTICIPANT))
                .linkedInstitutions(Set.of(UUID.randomUUID(), UUID.randomUUID()))
                .createdBy(UUID.randomUUID())
                .updatedBy(UUID.randomUUID())
                .build();
    }

    private UserResponse response(UUID id) {
        return UserResponse.builder()
                .id(id)
                .email("user@domain.com")
                .roles(Set.of("PARTICIPANT"))
                .build();
    }
}
