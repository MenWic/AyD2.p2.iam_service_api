package ayd2.p2b.iam_service_api.unit.feature.user.can_be_committee;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.feature.user.application.can_be_committee.CanBeCommitteeUseCase;
import ayd2.p2b.iam_service_api.feature.user.application.port.UserRepositoryPort;
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import ayd2.p2b.iam_service_api.feature.user.dto.internal.RequesterContext;
import ayd2.p2b.iam_service_api.feature.user.dto.response.CommitteeEligibilityResponse;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CanBeCommitteeUseCaseTest {

    @Mock private UserRepositoryPort userRepository;

    private CanBeCommitteeUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CanBeCommitteeUseCase(userRepository);
    }

    @Test
    void should_return_eligible_true_when_user_is_active_participant() {
        UUID targetId = UUID.randomUUID();
        when(userRepository.findById(targetId))
                .thenReturn(Optional.of(user(targetId, true, Set.of(Role.PARTICIPANT))));

        CommitteeEligibilityResponse result = useCase.execute(requester(Role.CONGRESS_ADMIN), targetId);

        assertTrue(result.isEligible());
    }

    @Test
    void should_return_eligible_true_when_user_is_active_congress_admin_with_participant_role() {
        UUID targetId = UUID.randomUUID();
        when(userRepository.findById(targetId))
                .thenReturn(Optional.of(user(targetId, true, Set.of(Role.CONGRESS_ADMIN, Role.PARTICIPANT))));

        CommitteeEligibilityResponse result = useCase.execute(requester(Role.CONGRESS_ADMIN), targetId);

        assertTrue(result.isEligible());
    }

    @Test
    void should_return_eligible_false_when_user_is_inactive_participant() {
        UUID targetId = UUID.randomUUID();
        when(userRepository.findById(targetId))
                .thenReturn(Optional.of(user(targetId, false, Set.of(Role.PARTICIPANT))));

        CommitteeEligibilityResponse result = useCase.execute(requester(Role.CONGRESS_ADMIN), targetId);

        assertFalse(result.isEligible());
    }

    @Test
    void should_return_eligible_false_when_user_has_no_participant_role() {
        UUID targetId = UUID.randomUUID();
        when(userRepository.findById(targetId))
                .thenReturn(Optional.of(user(targetId, true, Set.of(Role.CONGRESS_ADMIN))));

        CommitteeEligibilityResponse result = useCase.execute(requester(Role.CONGRESS_ADMIN), targetId);

        assertFalse(result.isEligible());
    }

    @Test
    void should_return_eligible_false_when_user_is_guest_speaker_only() {
        UUID targetId = UUID.randomUUID();
        when(userRepository.findById(targetId))
                .thenReturn(Optional.of(user(targetId, true, Set.of(Role.GUEST_SPEAKER))));

        CommitteeEligibilityResponse result = useCase.execute(requester(Role.CONGRESS_ADMIN), targetId);

        assertFalse(result.isEligible());
    }

    @Test
    void should_return_eligible_false_when_user_does_not_exist() {
        UUID targetId = UUID.randomUUID();
        when(userRepository.findById(targetId)).thenReturn(Optional.empty());

        CommitteeEligibilityResponse result = useCase.execute(requester(Role.CONGRESS_ADMIN), targetId);

        assertFalse(result.isEligible());
    }

    @Test
    void should_throw_forbidden_when_requester_is_system_admin() {
        UUID targetId = UUID.randomUUID();

        ApiException ex = assertThrows(
                ApiException.class,
                () -> useCase.execute(requester(Role.SYSTEM_ADMIN), targetId)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("auth.forbidden", ex.getCode());
    }

    @Test
    void should_throw_forbidden_when_requester_is_not_congress_admin() {
        UUID targetId = UUID.randomUUID();

        ApiException ex = assertThrows(
                ApiException.class,
                () -> useCase.execute(requester(Role.PARTICIPANT), targetId)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("auth.forbidden", ex.getCode());
    }

    @Test
    void should_throw_forbidden_when_requester_is_null() {
        ApiException ex = assertThrows(
                ApiException.class,
                () -> useCase.execute(null, UUID.randomUUID())
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("auth.forbidden", ex.getCode());
    }

    @Test
    void should_throw_validation_failed_when_target_user_id_is_null() {
        ApiException ex = assertThrows(
                ApiException.class,
                () -> useCase.execute(requester(Role.CONGRESS_ADMIN), null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("validation.failed", ex.getCode());
    }

    private RequesterContext requester(Role role) {
        return RequesterContext.builder()
                .userId(UUID.randomUUID())
                .roles(Set.of(role))
                .build();
    }

    private UserAccount user(UUID id, boolean active, Set<Role> roles) {
        return UserAccount.builder()
                .id(id)
                .email("user@domain.com")
                .active(active)
                .roles(roles)
                .build();
    }
}
