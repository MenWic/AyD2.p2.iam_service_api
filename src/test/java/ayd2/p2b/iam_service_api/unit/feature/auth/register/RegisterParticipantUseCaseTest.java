package ayd2.p2b.iam_service_api.unit.feature.auth.register;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.feature.auth.application.port.TokenIssuerPort;
import ayd2.p2b.iam_service_api.feature.auth.application.register.RegisterParticipantUseCase;
import ayd2.p2b.iam_service_api.feature.auth.dto.response.AuthResponse;
import ayd2.p2b.iam_service_api.feature.user.application.port.UserRepositoryPort;
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import ayd2.p2b.iam_service_api.feature.user.dto.request.RegisterUserRequest;
import ayd2.p2b.iam_service_api.feature.user.dto.response.UserResponse;
import ayd2.p2b.iam_service_api.feature.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RegisterParticipantUseCaseTest {

    @Mock private UserRepositoryPort userRepository;
    @Mock private UserMapper userMapper;
    @Mock private TokenIssuerPort tokenIssuerPort;

    private RegisterParticipantUseCase useCase;

    @BeforeEach
    void setUp() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        useCase = new RegisterParticipantUseCase(userRepository, userMapper, passwordEncoder, tokenIssuerPort);
    }

    @Test
    void should_register_participant_with_normalized_identity_fields() {
        RegisterUserRequest request = request();
        request.setEmail("  Participant@Domain.COM  ");
        request.setPersonalId("  A123B  ");
        request.setFullName("  Participant User  ");
        request.setOrganization("  Code n Bugs  ");
        request.setPhone("  555-0101  ");
        request.setPhotoUrl("  https://cdn.domain.com/a.png  ");

        given(userRepository.existsByEmailIgnoreCase("participant@domain.com")).willReturn(false);
        given(userRepository.existsByPersonalIdIgnoreCase("A123B")).willReturn(false);
        given(userRepository.save(any(UserAccount.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(userMapper.toResponse(any(UserAccount.class))).willReturn(userResponse());
        given(tokenIssuerPort.generateAccessToken(any(UserAccount.class))).willReturn("access");
        given(tokenIssuerPort.generateRefreshToken(any(UserAccount.class))).willReturn("refresh");

        AuthResponse response = useCase.execute(request);

        ArgumentCaptor<UserAccount> userCaptor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userRepository).save(userCaptor.capture());
        UserAccount saved = userCaptor.getValue();

        verify(userRepository).existsByEmailIgnoreCase("participant@domain.com");
        verify(userRepository).existsByPersonalIdIgnoreCase("A123B");
        assertEquals("participant@domain.com", saved.getEmail());
        assertEquals("A123B", saved.getPersonalId());
        assertEquals("Participant User", saved.getFullName());
        assertEquals("Code n Bugs", saved.getOrganization());
        assertEquals("555-0101", saved.getPhone());
        assertEquals("https://cdn.domain.com/a.png", saved.getPhotoUrl());
        assertTrue(saved.getRoles().contains(Role.PARTICIPANT));
        assertNotNull(response.getUser());
        assertEquals("access", response.getAccessToken());
    }

    @Test
    void should_fail_registration_when_email_already_exists() {
        RegisterUserRequest request = request();
        given(userRepository.existsByEmailIgnoreCase("participant@domain.com")).willReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> useCase.execute(request));

        assertEquals("resource.conflict", exception.getCode());
        verify(userRepository, never()).save(any(UserAccount.class));
    }

    @Test
    void should_fail_registration_when_personal_id_already_exists() {
        RegisterUserRequest request = request();
        request.setPersonalId("  A123B  ");
        given(userRepository.existsByEmailIgnoreCase("participant@domain.com")).willReturn(false);
        given(userRepository.existsByPersonalIdIgnoreCase("A123B")).willReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> useCase.execute(request));

        assertEquals("resource.conflict", exception.getCode());
        verify(userRepository, never()).save(any(UserAccount.class));
    }

    @Test
    void should_fail_registration_when_personal_id_format_is_invalid_before_repository_queries() {
        RegisterUserRequest request = request();
        request.setPersonalId("A-123");

        ApiException exception = assertThrows(ApiException.class, () -> useCase.execute(request));

        assertEquals("validation.failed", exception.getCode());
        verifyNoInteractions(userRepository);
    }

    @Test
    void should_keep_photo_url_null_when_not_provided() {
        RegisterUserRequest request = request();
        request.setPhotoUrl(null);
        given(userRepository.existsByEmailIgnoreCase("participant@domain.com")).willReturn(false);
        given(userRepository.existsByPersonalIdIgnoreCase("A123B")).willReturn(false);
        given(userRepository.save(any(UserAccount.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(userMapper.toResponse(any(UserAccount.class))).willReturn(userResponse());
        given(tokenIssuerPort.generateAccessToken(any(UserAccount.class))).willReturn("access");
        given(tokenIssuerPort.generateRefreshToken(any(UserAccount.class))).willReturn("refresh");

        useCase.execute(request);

        ArgumentCaptor<UserAccount> userCaptor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userRepository).save(userCaptor.capture());
        assertNull(userCaptor.getValue().getPhotoUrl());
    }

    private RegisterUserRequest request() {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setEmail("participant@domain.com");
        request.setPassword("MyStrongPassword123");
        request.setFullName("Participant User");
        request.setOrganization("Code n Bugs");
        request.setPhone("555-0101");
        request.setPersonalId("A123B");
        request.setPhotoUrl("https://cdn.domain.com/a.png");
        return request;
    }

    private UserResponse userResponse() {
        return UserResponse.builder()
                .id(UUID.randomUUID())
                .email("participant@domain.com")
                .fullName("Participant User")
                .organization("Code n Bugs")
                .phone("555-0101")
                .personalId("A123B")
                .active(true)
                .roles(Set.of("PARTICIPANT"))
                .build();
    }
}
