package ayd2.p2b.iam_service_api.feature.user.application.can_be_committee;

import ayd2.p2b.iam_service_api.feature.user.application.port.UserRepositoryPort;
import ayd2.p2b.iam_service_api.feature.user.application.exception.UserExceptions;
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import ayd2.p2b.iam_service_api.feature.user.dto.internal.RequesterContext;
import ayd2.p2b.iam_service_api.feature.user.dto.response.CommitteeEligibilityResponse;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class CanBeCommitteeUseCase {

    private final UserRepositoryPort userRepository;

    public CanBeCommitteeUseCase(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public CommitteeEligibilityResponse execute(RequesterContext requester, UUID targetUserId) {
        if (targetUserId == null) {
            throw UserExceptions.validationFailed("targetUserId is required");
        }
        requireAuthorized(requester);

        boolean eligible = userRepository.findById(targetUserId)
                .map(this::isEligible)
                .orElse(false);

        return CommitteeEligibilityResponse.builder()
                .eligible(eligible)
                .build();
    }

    private void requireAuthorized(RequesterContext requester) {
        boolean isCongressAdmin = requester != null
                && requester.getUserId() != null
                && requester.getRoles() != null
                && requester.getRoles().contains(Role.CONGRESS_ADMIN);

        if (!isCongressAdmin) {
            throw UserExceptions.forbidden();
        }
    }

    private boolean isEligible(UserAccount userAccount) {
        return Boolean.TRUE.equals(userAccount.getActive())
                && userAccount.getRoles() != null
                && userAccount.getRoles().contains(Role.PARTICIPANT);
    }
}
