package ayd2.p2b.iam_service_api.unit.feature.user.create_guest_speaker;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.feature.user.application.create_guest_speaker.CreateGuestSpeakerUseCase;
import ayd2.p2b.iam_service_api.feature.user.application.port.UserRepositoryPort;
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import ayd2.p2b.iam_service_api.feature.user.dto.internal.RequesterContext;
import ayd2.p2b.iam_service_api.feature.user.dto.request.CreateGuestSpeakerRequest;
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
class CreateGuestSpeakerUseCaseTest {

    @Mock private UserRepositoryPort userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordHasherPort passwordHasher;

    private CreateGuestSpeakerUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateGuestSpeakerUseCase(userRepository, userMapper, passwordHasher);
    }

    @Test
    void should_create_guest_speaker_with_guest_speaker_role_only() {
        RequesterContext requester = requester(Role.CONGRESS_ADMIN);
        CreateGuestSpeakerRequest request = validRequest();

        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(userRepository.existsByPersonalIdIgnoreCase(any())).thenReturn(false);
        when(passwordHasher.encode("Password123")).thenReturn("hashed");
        when(userRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponse(any(UserAccount.class))).thenReturn(response());

        useCase.execute(requester, request);

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userRepository).save(captor.capture());
        assertEquals(Set.of(Role.GUEST_SPEAKER), captor.getValue().getRoles());
    }

    @Test
    void should_allow_null_password_hash_when_password_missing() {
        RequesterContext requester = requester(Role.CONGRESS_ADMIN);
        CreateGuestSpeakerRequest request = validRequest();
        request.setPassword(null);

        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(userRepository.existsByPersonalIdIgnoreCase(any())).thenReturn(false);
        when(userRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponse(any(UserAccount.class))).thenReturn(response());

        useCase.execute(requester, request);

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userRepository).save(captor.capture());
        assertNull(captor.getValue().getPasswordHash());
        verify(passwordHasher, never()).encode(any());
    }

    @Test
    void should_encode_password_when_password_is_present() {
        RequesterContext requester = requester(Role.CONGRESS_ADMIN);
        CreateGuestSpeakerRequest request = validRequest();
        request.setPassword("Password123");

        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(userRepository.existsByPersonalIdIgnoreCase(any())).thenReturn(false);
        when(passwordHasher.encode("Password123")).thenReturn("hashed");
        when(userRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponse(any(UserAccount.class))).thenReturn(response());

        useCase.execute(requester, request);

        verify(passwordHasher).encode("Password123");
    }

    @Test
    void should_throw_forbidden_when_requester_is_not_congress_admin() {
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
        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(requester(Role.CONGRESS_ADMIN), null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("validation.failed", ex.getCode());
    }

    @Test
    void should_throw_conflict_when_email_exists() {
        RequesterContext requester = requester(Role.CONGRESS_ADMIN);
        CreateGuestSpeakerRequest request = validRequest();

        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(requester, request));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("resource.conflict", ex.getCode());
        verify(userRepository, never()).save(any(UserAccount.class));
    }

    @Test
    void should_throw_conflict_when_personal_id_exists() {
        RequesterContext requester = requester(Role.CONGRESS_ADMIN);
        CreateGuestSpeakerRequest request = validRequest();

        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(userRepository.existsByPersonalIdIgnoreCase(any())).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(requester, request));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("resource.conflict", ex.getCode());
        verify(userRepository, never()).save(any(UserAccount.class));
    }

    @Test
    void should_set_active_created_by_and_updated_by() {
        UUID requesterId = UUID.randomUUID();
        RequesterContext requester = RequesterContext.builder()
                .userId(requesterId)
                .roles(Set.of(Role.CONGRESS_ADMIN))
                .build();
        CreateGuestSpeakerRequest request = validRequest();

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
        RequesterContext requester = requester(Role.CONGRESS_ADMIN);
        CreateGuestSpeakerRequest request = validRequest();
        request.setEmail("  GUEST@DOMAIN.COM  ");
        request.setPersonalId("  A123B  ");

        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(userRepository.existsByPersonalIdIgnoreCase(any())).thenReturn(false);
        when(passwordHasher.encode(any())).thenReturn("hashed");
        when(userRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponse(any(UserAccount.class))).thenReturn(response());

        useCase.execute(requester, request);

        verify(userRepository).existsByEmailIgnoreCase(eq("guest@domain.com"));
        verify(userRepository).existsByPersonalIdIgnoreCase(eq("A123B"));
    }

    @Test
    void should_keep_password_hash_null_when_password_is_blank() {
        RequesterContext requester = requester(Role.CONGRESS_ADMIN);
        CreateGuestSpeakerRequest request = validRequest();
        request.setPassword("   ");

        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(userRepository.existsByPersonalIdIgnoreCase(any())).thenReturn(false);
        when(userRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponse(any(UserAccount.class))).thenReturn(response());

        useCase.execute(requester, request);

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userRepository).save(captor.capture());
        assertNull(captor.getValue().getPasswordHash());
        verify(passwordHasher, never()).encode(any());
    }

    @Test
    void should_reject_invalid_personal_id_format() {
        RequesterContext requester = requester(Role.CONGRESS_ADMIN);
        CreateGuestSpeakerRequest request = validRequest();
        request.setPersonalId("A-123");

        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(requester, request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("validation.failed", ex.getCode());
    }

    @Test
    void should_throw_validation_failed_when_password_is_nonblank_and_too_short() {
        RequesterContext requester = requester(Role.CONGRESS_ADMIN);
        CreateGuestSpeakerRequest request = validRequest();
        request.setPassword("short");

        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(userRepository.existsByPersonalIdIgnoreCase(any())).thenReturn(false);

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

    private CreateGuestSpeakerRequest validRequest() {
        CreateGuestSpeakerRequest request = new CreateGuestSpeakerRequest();
        request.setEmail("guestspeaker@domain.com");
        request.setFullName("Guest Speaker");
        request.setOrganization("Code n Bugs");
        request.setPhone("555-0101");
        request.setPersonalId("A123B");
        request.setPhotoUrl("https://cdn.domain.com/p.png");
        request.setPassword("Password123");
        return request;
    }

    private UserResponse response() {
        return UserResponse.builder()
                .id(UUID.randomUUID())
                .email("guestspeaker@domain.com")
                .roles(Set.of("GUEST_SPEAKER"))
                .build();
    }
}
