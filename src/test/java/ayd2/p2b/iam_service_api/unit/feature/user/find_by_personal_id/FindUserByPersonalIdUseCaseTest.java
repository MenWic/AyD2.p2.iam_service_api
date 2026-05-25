package ayd2.p2b.iam_service_api.unit.feature.user.find_by_personal_id;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.feature.user.application.find_by_personal_id.FindUserByPersonalIdUseCase;
import ayd2.p2b.iam_service_api.feature.user.application.port.UserRepositoryPort;
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import ayd2.p2b.iam_service_api.feature.user.dto.response.InternalUserIdentityResponse;
import ayd2.p2b.iam_service_api.feature.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindUserByPersonalIdUseCaseTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private UserMapper userMapper;

    private FindUserByPersonalIdUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindUserByPersonalIdUseCase(userRepository, userMapper);
    }

    @Test
    void should_return_identity_with_canonical_personal_id_when_one_active_user_matches_case_insensitive() {
        UUID userId = UUID.randomUUID();
        UserAccount stored = activeUser(userId, "AbC123");
        InternalUserIdentityResponse mapped = InternalUserIdentityResponse.builder()
                .id(userId)
                .personalId("AbC123")
                .build();

        when(userRepository.findActiveUsersByPersonalIdIgnoreCase("abc123")).thenReturn(List.of(stored));
        when(userMapper.toInternalIdentityResponse(stored)).thenReturn(mapped);

        InternalUserIdentityResponse result = useCase.execute("  abc123 ");

        assertEquals(userId, result.getId());
        assertEquals("AbC123", result.getPersonalId());
        verify(userRepository).findActiveUsersByPersonalIdIgnoreCase("abc123");
        verify(userMapper).toInternalIdentityResponse(stored);
    }

    @Test
    void should_throw_not_found_when_no_active_user_matches_personal_id() {
        when(userRepository.findActiveUsersByPersonalIdIgnoreCase("abc123")).thenReturn(List.of());

        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute("abc123"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("resource.not_found", ex.getCode());
    }

    @Test
    void should_throw_not_found_when_only_inactive_user_exists_because_repository_returns_active_only() {
        when(userRepository.findActiveUsersByPersonalIdIgnoreCase("inactive001")).thenReturn(List.of());

        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute("inactive001"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("resource.not_found", ex.getCode());
    }

    @Test
    void should_throw_conflict_when_more_than_one_active_user_matches_personal_id_case_insensitively() {
        when(userRepository.findActiveUsersByPersonalIdIgnoreCase("dup001"))
                .thenReturn(List.of(
                        activeUser(UUID.randomUUID(), "Dup001"),
                        activeUser(UUID.randomUUID(), "dUP001")
                ));

        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute("dup001"));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("resource.conflict", ex.getCode());
    }

    @Test
    void should_throw_validation_failed_when_personal_id_is_blank() {
        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute("   "));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("validation.failed", ex.getCode());
    }

    @Test
    void should_throw_validation_failed_when_personal_id_has_invalid_format() {
        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute("A-123"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("validation.failed", ex.getCode());
    }

    private UserAccount activeUser(UUID id, String personalId) {
        return UserAccount.builder()
                .id(id)
                .email(personalId + "@domain.com")
                .fullName("User " + personalId)
                .organization("Code n Bugs")
                .phone("555-0101")
                .personalId(personalId)
                .active(true)
                .roles(Set.of(Role.PARTICIPANT))
                .build();
    }
}
