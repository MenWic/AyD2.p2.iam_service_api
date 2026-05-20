package ayd2.p2b.iam_service_api.unit.feature.user.create_system_admin;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.feature.user.application.create_system_admin.CreateSystemAdminUseCase;
import ayd2.p2b.iam_service_api.feature.user.application.port.UserRepositoryPort;
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import ayd2.p2b.iam_service_api.feature.user.dto.internal.RequesterContext;
import ayd2.p2b.iam_service_api.feature.user.dto.request.CreateSystemAdminRequest;
import ayd2.p2b.iam_service_api.feature.user.dto.response.UserResponse;
import ayd2.p2b.iam_service_api.feature.user.mapper.UserMapper;
import ayd2.p2b.iam_service_api.feature.auth.application.port.PasswordHasherPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateSystemAdminUseCaseTest {

    @Mock private UserRepositoryPort userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordHasherPort passwordHasher;

    private CreateSystemAdminUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateSystemAdminUseCase(userRepository, userMapper, passwordHasher);
    }

    @Test
    void should_create_system_admin_with_system_admin_and_participant_roles() {
        RequesterContext requester = requester(Role.SYSTEM_ADMIN);
        CreateSystemAdminRequest request = validRequest();
        UserResponse response = response();

        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(userRepository.existsByPersonalIdIgnoreCase(any())).thenReturn(false);
        when(passwordHasher.encode("Password123")).thenReturn("hashed");
        when(userRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponse(any(UserAccount.class))).thenReturn(response);

        useCase.execute(requester, request);

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userRepository).save(captor.capture());
        assertTrue(captor.getValue().getRoles().contains(Role.SYSTEM_ADMIN));
        assertTrue(captor.getValue().getRoles().contains(Role.PARTICIPANT));
    }

    @Test
    void should_throw_conflict_when_email_exists() {
        RequesterContext requester = requester(Role.SYSTEM_ADMIN);
        CreateSystemAdminRequest request = validRequest();

        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(requester, request));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("resource.conflict", ex.getCode());
        verify(userRepository, never()).save(any(UserAccount.class));
    }

    @Test
    void should_throw_conflict_when_personal_id_exists() {
        RequesterContext requester = requester(Role.SYSTEM_ADMIN);
        CreateSystemAdminRequest request = validRequest();

        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(userRepository.existsByPersonalIdIgnoreCase(any())).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(requester, request));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("resource.conflict", ex.getCode());
        verify(userRepository, never()).save(any(UserAccount.class));
    }

    @Test
    void should_throw_forbidden_when_requester_is_not_system_admin() {
        RequesterContext requester = requester(Role.PARTICIPANT);

        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(requester, validRequest()));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("auth.forbidden", ex.getCode());
    }

    @Test
    void should_throw_forbidden_when_requester_is_null() {
        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(null, validRequest()));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("auth.forbidden", ex.getCode());
    }

    @Test
    void should_throw_validation_failed_when_request_is_null() {
        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(requester(Role.SYSTEM_ADMIN), null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("validation.failed", ex.getCode());
    }

    @Test
    void should_encode_password() {
        RequesterContext requester = requester(Role.SYSTEM_ADMIN);
        CreateSystemAdminRequest request = validRequest();

        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(userRepository.existsByPersonalIdIgnoreCase(any())).thenReturn(false);
        when(passwordHasher.encode("Password123")).thenReturn("hashed");
        when(userRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponse(any(UserAccount.class))).thenReturn(response());

        useCase.execute(requester, request);

        verify(passwordEncoder).encode("Password123");
    }

    @Test
    void should_set_active_created_by_and_updated_by() {
        UUID requesterId = UUID.randomUUID();
        RequesterContext requester = RequesterContext.builder()
                .userId(requesterId)
                .roles(Set.of(Role.SYSTEM_ADMIN))
                .build();
        CreateSystemAdminRequest request = validRequest();

        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(userRepository.existsByPersonalIdIgnoreCase(any())).thenReturn(false);
        when(passwordHasher.encode(any())).thenReturn("hashed");
        when(userRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponse(any(UserAccount.class))).thenReturn(response());

        useCase.execute(requester, request);

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userRepository).save(captor.capture());
        assertEquals(Boolean.TRUE, captor.getValue().getActive());
        assertEquals(requesterId, captor.getValue().getCreatedBy());
        assertEquals(requesterId, captor.getValue().getUpdatedBy());
    }

    @Test
    void should_normalize_email_and_trim_personal_id_before_save() {
        RequesterContext requester = requester(Role.SYSTEM_ADMIN);
        CreateSystemAdminRequest request = validRequest();
        request.setEmail("  ADMIN@DOMAIN.COM  ");
        request.setPersonalId("  A123B  ");
        request.setFullName("  System Admin  ");
        request.setOrganization("  Code n Bugs  ");
        request.setPhone("  555-0101  ");
        request.setPhotoUrl("   ");

        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(userRepository.existsByPersonalIdIgnoreCase(any())).thenReturn(false);
        when(passwordHasher.encode(any())).thenReturn("hashed");
        when(userRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponse(any(UserAccount.class))).thenReturn(response());

        useCase.execute(requester, request);

        verify(userRepository).existsByEmailIgnoreCase(eq("admin@domain.com"));
        verify(userRepository).existsByPersonalIdIgnoreCase(eq("A123B"));

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userRepository).save(captor.capture());
        assertEquals("admin@domain.com", captor.getValue().getEmail());
        assertEquals("A123B", captor.getValue().getPersonalId());
        assertEquals("System Admin", captor.getValue().getFullName());
        assertEquals("Code n Bugs", captor.getValue().getOrganization());
        assertEquals("555-0101", captor.getValue().getPhone());
        assertNull(captor.getValue().getPhotoUrl());
    }

    @Test
    void should_reject_invalid_personal_id_format() {
        RequesterContext requester = requester(Role.SYSTEM_ADMIN);
        CreateSystemAdminRequest request = validRequest();
        request.setPersonalId("A-123");

        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(requester, request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("validation.failed", ex.getCode());
    }

    private RequesterContext requester(Role role) {
        return RequesterContext.builder()
                .userId(UUID.randomUUID())
                .roles(Set.of(role))
                .build();
    }

    private CreateSystemAdminRequest validRequest() {
        CreateSystemAdminRequest request = new CreateSystemAdminRequest();
        request.setEmail("admin@domain.com");
        request.setPassword("Password123");
        request.setFullName("System Admin");
        request.setOrganization("Code n Bugs");
        request.setPhone("555-0101");
        request.setPersonalId("A123B");
        request.setPhotoUrl("https://cdn.domain.com/p.png");
        return request;
    }

    private UserResponse response() {
        return UserResponse.builder()
                .id(UUID.randomUUID())
                .email("admin@domain.com")
                .roles(Set.of("SYSTEM_ADMIN", "PARTICIPANT"))
                .build();
    }
}
