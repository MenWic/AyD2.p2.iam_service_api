package ayd2.p2b.iam_service_api.unit.usecase.auth;

import ayd2.p2b.iam_service_api.application.dto.auth.AuthResponse;
import ayd2.p2b.iam_service_api.application.dto.user.RegisterUserRequest;
import ayd2.p2b.iam_service_api.application.dto.user.UserResponse;
import ayd2.p2b.iam_service_api.application.mapper.user.UserMapper;
import ayd2.p2b.iam_service_api.application.port.security.TokenIssuerPort;
import ayd2.p2b.iam_service_api.application.port.user.UserRepositoryPort;
import ayd2.p2b.iam_service_api.application.usecase.auth.RegisterParticipantUseCase;
import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.domain.model.user.Role;
import ayd2.p2b.iam_service_api.domain.model.user.UserAccount;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegisterParticipantUseCaseTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private TokenIssuerPort tokenIssuerPort;

    private RegisterParticipantUseCase useCase;

    @BeforeEach
    void setUp() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        useCase = new RegisterParticipantUseCase(userRepository, userMapper, passwordEncoder, tokenIssuerPort);
    }

    @Test
    void should_register_participant_when_request_is_valid() {
        RegisterUserRequest request = request();
        given(userRepository.existsByEmailIgnoreCase("participant@domain.com")).willReturn(false);
        given(userRepository.save(any(UserAccount.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(userMapper.toResponse(any(UserAccount.class))).willReturn(userResponse());
        given(tokenIssuerPort.generateAccessToken(any(UserAccount.class))).willReturn("access");
        given(tokenIssuerPort.generateRefreshToken(any(UserAccount.class))).willReturn("refresh");

        AuthResponse response = useCase.execute(request);

        ArgumentCaptor<UserAccount> userCaptor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userRepository).save(userCaptor.capture());
        assertTrue(userCaptor.getValue().getRoles().contains(Role.PARTICIPANT));
        assertNotNull(response.getUser());
        assertEquals("access", response.getAccessToken());
    }

    @Test
    void should_fail_registration_when_email_already_exists() {
        RegisterUserRequest request = request();
        given(userRepository.existsByEmailIgnoreCase("participant@domain.com")).willReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> useCase.execute(request));

        assertEquals("resource.conflict", exception.getCode());
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
