package ayd2.p2b.iam_service_api.unit.feature.user.create_congress_admin;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.feature.user.application.create_congress_admin.CreateCongressAdminUseCase;
import ayd2.p2b.iam_service_api.feature.user.application.port.UserRepositoryPort;
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import ayd2.p2b.iam_service_api.feature.user.dto.internal.RequesterContext;
import ayd2.p2b.iam_service_api.feature.user.dto.request.CreateCongressAdminRequest;
import ayd2.p2b.iam_service_api.feature.user.dto.response.UserResponse;
import ayd2.p2b.iam_service_api.feature.user.mapper.UserMapper;
import ayd2.p2b.iam_service_api.core.security.password.PasswordHasherPort;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCongressAdminUseCaseTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordHasherPort passwordHasher;

    private CreateCongressAdminUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateCongressAdminUseCase(userRepository, userMapper, passwordHasher);
    }

    @Test
    void should_create_congress_admin_with_congress_admin_and_participant_roles() {
        RequesterContext requester = requester(Role.SYSTEM_ADMIN);
        CreateCongressAdminRequest request = validRequest();

        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(userRepository.existsByPersonalIdIgnoreCase(any())).thenReturn(false);
        when(passwordHasher.encode("Password123")).thenReturn("hashed");
        when(userRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponse(any(UserAccount.class))).thenReturn(response());

        useCase.execute(requester, request);

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userRepository).save(captor.capture());
        assertTrue(captor.getValue().getRoles().contains(Role.CONGRESS_ADMIN));
        assertTrue(captor.getValue().getRoles().contains(Role.PARTICIPANT));
    }

    @Test
    void should_store_linked_institutions_from_request() {
        RequesterContext requester = requester(Role.SYSTEM_ADMIN);
        CreateCongressAdminRequest request = validRequest();
        UUID institutionA = UUID.randomUUID();
        UUID institutionB = UUID.randomUUID();
        request.setInstitutionIds(Set.of(institutionA, institutionB));

        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(userRepository.existsByPersonalIdIgnoreCase(any())).thenReturn(false);
        when(passwordHasher.encode(any())).thenReturn("hashed");
        when(userRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponse(any(UserAccount.class))).thenReturn(response());

        useCase.execute(requester, request);

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userRepository).save(captor.capture());
        assertEquals(Set.of(institutionA, institutionB), captor.getValue().getLinkedInstitutions());
    }

    @Test
    void should_throw_conflict_when_email_exists() {
        RequesterContext requester = requester(Role.SYSTEM_ADMIN);
        CreateCongressAdminRequest request = validRequest();

        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(requester, request));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("resource.conflict", ex.getCode());
        verify(userRepository, never()).save(any(UserAccount.class));
    }

    @Test
    void should_throw_conflict_when_personal_id_exists() {
        RequesterContext requester = requester(Role.SYSTEM_ADMIN);
        CreateCongressAdminRequest request = validRequest();

        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(userRepository.existsByPersonalIdIgnoreCase(any())).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(requester, request));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("resource.conflict", ex.getCode());
        verify(userRepository, never()).save(any(UserAccount.class));
    }

    @Test
    void should_throw_forbidden_when_requester_is_not_system_admin() {
        RequesterContext requester = requester(Role.CONGRESS_ADMIN);

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
    void should_throw_forbidden_when_requester_roles_are_null_or_empty() {
        RequesterContext nullRoles = RequesterContext.builder()
                .userId(UUID.randomUUID())
                .roles(null)
                .build();
        RequesterContext emptyRoles = RequesterContext.builder()
                .userId(UUID.randomUUID())
                .roles(Set.of())
                .build();

        ApiException nullRolesEx = assertThrows(ApiException.class, () -> useCase.execute(nullRoles, validRequest()));
        ApiException emptyRolesEx = assertThrows(ApiException.class, () -> useCase.execute(emptyRoles, validRequest()));

        assertEquals(HttpStatus.FORBIDDEN, nullRolesEx.getStatus());
        assertEquals("auth.forbidden", nullRolesEx.getCode());
        assertEquals(HttpStatus.FORBIDDEN, emptyRolesEx.getStatus());
        assertEquals("auth.forbidden", emptyRolesEx.getCode());
    }

    @Test
    void should_throw_validation_failed_when_request_is_null() {
        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(requester(Role.SYSTEM_ADMIN), null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("validation.failed", ex.getCode());
    }

    @Test
    void should_throw_validation_failed_when_institution_ids_are_empty() {
        CreateCongressAdminRequest request = validRequest();
        request.setInstitutionIds(Set.of());

        ApiException ex = assertThrows(
                ApiException.class,
                () -> useCase.execute(requester(Role.SYSTEM_ADMIN), request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("validation.failed", ex.getCode());
    }

    @Test
    void should_encode_password() {
        RequesterContext requester = requester(Role.SYSTEM_ADMIN);
        CreateCongressAdminRequest request = validRequest();

        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(userRepository.existsByPersonalIdIgnoreCase(any())).thenReturn(false);
        when(passwordHasher.encode("Password123")).thenReturn("hashed");
        when(userRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponse(any(UserAccount.class))).thenReturn(response());

        useCase.execute(requester, request);

        verify(passwordHasher).encode("Password123");
    }

    @Test
    void should_set_active_created_by_and_updated_by() {
        UUID requesterId = UUID.randomUUID();
        RequesterContext requester = RequesterContext.builder()
                .userId(requesterId)
                .roles(Set.of(Role.SYSTEM_ADMIN))
                .build();
        CreateCongressAdminRequest request = validRequest();

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
        CreateCongressAdminRequest request = validRequest();
        request.setEmail("  CONGRESS@DOMAIN.COM  ");
        request.setPersonalId("  A123B  ");

        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(userRepository.existsByPersonalIdIgnoreCase(any())).thenReturn(false);
        when(passwordHasher.encode(any())).thenReturn("hashed");
        when(userRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponse(any(UserAccount.class))).thenReturn(response());

        useCase.execute(requester, request);

        verify(userRepository).existsByEmailIgnoreCase(eq("congress@domain.com"));
        verify(userRepository).existsByPersonalIdIgnoreCase(eq("A123B"));

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userRepository).save(captor.capture());
        assertEquals("congress@domain.com", captor.getValue().getEmail());
        assertEquals("A123B", captor.getValue().getPersonalId());
    }

    @Test
    void should_reject_invalid_personal_id_format() {
        RequesterContext requester = requester(Role.SYSTEM_ADMIN);
        CreateCongressAdminRequest request = validRequest();
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

    private CreateCongressAdminRequest validRequest() {
        CreateCongressAdminRequest request = new CreateCongressAdminRequest();
        request.setEmail("congressadmin@domain.com");
        request.setPassword("Password123");
        request.setFullName("Congress Admin");
        request.setOrganization("Code n Bugs");
        request.setPhone("555-0101");
        request.setPersonalId("A123B");
        request.setPhotoUrl("https://cdn.domain.com/p.png");
        request.setInstitutionIds(Set.of(UUID.randomUUID()));
        return request;
    }

    private UserResponse response() {
        return UserResponse.builder()
                .id(UUID.randomUUID())
                .email("congressadmin@domain.com")
                .roles(Set.of("CONGRESS_ADMIN", "PARTICIPANT"))
                .build();
    }
}
