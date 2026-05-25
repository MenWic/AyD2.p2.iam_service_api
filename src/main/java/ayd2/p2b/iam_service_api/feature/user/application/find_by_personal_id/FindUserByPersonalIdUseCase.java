package ayd2.p2b.iam_service_api.feature.user.application.find_by_personal_id;

import ayd2.p2b.iam_service_api.common.util.TextNormalizer;
import ayd2.p2b.iam_service_api.common.validation.PersonalIdValidator;
import ayd2.p2b.iam_service_api.feature.user.application.exception.UserExceptions;
import ayd2.p2b.iam_service_api.feature.user.application.port.UserRepositoryPort;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import ayd2.p2b.iam_service_api.feature.user.dto.response.InternalUserIdentityResponse;
import ayd2.p2b.iam_service_api.feature.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FindUserByPersonalIdUseCase {

    private final UserRepositoryPort userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public InternalUserIdentityResponse execute(String personalId) {
        String normalizedPersonalId = TextNormalizer.trimToNull(personalId);
        if (!PersonalIdValidator.isAlphanumeric(normalizedPersonalId)) {
            throw UserExceptions.invalidPersonalId();
        }

        List<UserAccount> users = userRepository.findActiveUsersByPersonalIdIgnoreCase(normalizedPersonalId);
        if (users.isEmpty()) {
            throw UserExceptions.notFound();
        }
        if (users.size() > 1) {
            throw UserExceptions.multipleActiveUsersForPersonalId();
        }

        return userMapper.toInternalIdentityResponse(users.getFirst());
    }
}
