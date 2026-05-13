package ayd2.p2b.iam_service_api.unit.feature.auth.login;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.feature.auth.application.login.LoginUseCase;
import ayd2.p2b.iam_service_api.feature.auth.application.port.TokenIssuerPort;
import ayd2.p2b.iam_service_api.feature.auth.dto.request.LoginRequest;
import ayd2.p2b.iam_service_api.feature.auth.dto.response.AuthResponse;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock private UserRepositoryPort userRepository;
    @Mock private UserMapper userMapper;
    @Mock private TokenIssuerPort tokenIssuerPort;

    private PasswordEncoder passwordEncoder;
    private LoginUseCase useCase;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        useCase = new LoginUseCase(userRepository, userMapper, passwordEncoder, tokenIssuerPort);
    }

    @Test
    void should_login_when_credentials_are_valid() {
        LoginRequest request = loginRequest("MyStrongPassword123");
        UserAccount user = activeUser();
        when(userRepository.findByEmailIgnoreCase("participant@domain.com")).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse());
        when(tokenIssuerPort.generateAccessToken(user)).thenReturn("access");
        when(tokenIssuerPort.generateRefreshToken(user)).thenReturn("refresh");

        AuthResponse response = useCase.execute(request);

        assertEquals("access", response.getAccessToken());
        assertEquals("refresh", response.getRefreshToken());
    }

    @Test
    void should_fail_login_when_credentials_are_invalid() {
        LoginRequest request = loginRequest("WrongPassword");
        when(userRepository.findByEmailIgnoreCase("participant@domain.com")).thenReturn(Optional.of(activeUser()));

        ApiException exception = assertThrows(ApiException.class, () -> useCase.execute(request));
        assertEquals("auth.invalid_credentials", exception.getCode());
    }

    @Test
    void should_fail_login_when_user_is_inactive() {
        LoginRequest request = loginRequest("MyStrongPassword123");
        UserAccount user = activeUser();
        user = UserAccount.builder()
                .id(user.getId())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .fullName(user.getFullName())
                .organization(user.getOrganization())
                .phone(user.getPhone())
                .personalId(user.getPersonalId())
                .photoUrl(user.getPhotoUrl())
                .active(false)
                .roles(user.getRoles())
                .build();
        when(userRepository.findByEmailIgnoreCase("participant@domain.com")).thenReturn(Optional.of(user));

        ApiException exception = assertThrows(ApiException.class, () -> useCase.execute(request));
        assertEquals("auth.invalid_credentials", exception.getCode());
    }

    @Test
    void should_fail_with_validation_failed_when_request_is_null() {
        ApiException exception = assertThrows(ApiException.class, () -> useCase.execute(null));

        assertEquals("validation.failed", exception.getCode());
    }

    @Test
    void should_fail_with_invalid_credentials_when_email_is_blank() {
        LoginRequest request = loginRequest("MyStrongPassword123");
        request.setEmail("   ");

        ApiException exception = assertThrows(ApiException.class, () -> useCase.execute(request));

        assertEquals("auth.invalid_credentials", exception.getCode());
        verify(userRepository, never()).findByEmailIgnoreCase("participant@domain.com");
    }

    @Test
    void should_fail_with_invalid_credentials_when_password_is_blank() {
        LoginRequest request = loginRequest("   ");

        ApiException exception = assertThrows(ApiException.class, () -> useCase.execute(request));

        assertEquals("auth.invalid_credentials", exception.getCode());
        verify(userRepository, never()).findByEmailIgnoreCase("participant@domain.com");
    }

    @Test
    void should_throw_domain_invariant_when_user_roles_are_null_or_empty() {
        LoginRequest request = loginRequest("MyStrongPassword123");
        UserAccount userWithNullRoles = activeUser().toBuilder().roles(null).build();
        UserAccount userWithEmptyRoles = activeUser().toBuilder().roles(Set.of()).build();

        when(userRepository.findByEmailIgnoreCase("participant@domain.com")).thenReturn(Optional.of(userWithNullRoles));

        ApiException nullRolesException = assertThrows(ApiException.class, () -> useCase.execute(request));
        assertEquals("domain.invariant_violated", nullRolesException.getCode());

        when(userRepository.findByEmailIgnoreCase("participant@domain.com")).thenReturn(Optional.of(userWithEmptyRoles));

        ApiException emptyRolesException = assertThrows(ApiException.class, () -> useCase.execute(request));
        assertEquals("domain.invariant_violated", emptyRolesException.getCode());
    }

    private LoginRequest loginRequest(String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail("participant@domain.com");
        request.setPassword(password);
        return request;
    }

    private UserAccount activeUser() {
        return UserAccount.builder()
                .id(UUID.randomUUID())
                .email("participant@domain.com")
                .passwordHash(passwordEncoder.encode("MyStrongPassword123"))
                .fullName("Participant User")
                .organization("Code n Bugs")
                .phone("555-0101")
                .personalId("A123B")
                .active(true)
                .roles(Set.of(Role.PARTICIPANT))
                .build();
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

