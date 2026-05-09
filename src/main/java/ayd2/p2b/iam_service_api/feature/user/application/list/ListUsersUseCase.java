package ayd2.p2b.iam_service_api.feature.user.application.list;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.common.response.PageResponse;
import ayd2.p2b.iam_service_api.feature.user.application.port.UserRepositoryPort;
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import ayd2.p2b.iam_service_api.feature.user.dto.internal.RequesterContext;
import ayd2.p2b.iam_service_api.feature.user.dto.internal.UserSearchCriteria;
import ayd2.p2b.iam_service_api.feature.user.dto.response.UserResponse;
import ayd2.p2b.iam_service_api.feature.user.mapper.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class ListUsersUseCase {

    private final UserRepositoryPort userRepository;
    private final UserMapper userMapper;

    public ListUsersUseCase(UserRepositoryPort userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> execute(RequesterContext requester, UserSearchCriteria criteria, Pageable pageable) {
        Pageable effectivePageable = pageable;
        UserSearchCriteria normalizedCriteria = criteria == null ? UserSearchCriteria.builder().build() : criteria;

        if (hasRole(requester, Role.SYSTEM_ADMIN)) {
            return fetchPage(normalizedCriteria, effectivePageable);
        }

        if (!hasRole(requester, Role.CONGRESS_ADMIN)) {
            throw forbidden();
        }

        UserAccount requesterAccount = userRepository.findById(requester.getUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "resource.not_found", "User not found"));

        Set<UUID> requesterInstitutions = requesterAccount.getLinkedInstitutions() == null
                ? Set.of()
                : requesterAccount.getLinkedInstitutions();

        if (requesterInstitutions.isEmpty()) {
            return new PageResponse<>(
                    List.of(),
                    effectivePageable.getPageNumber(),
                    effectivePageable.getPageSize(),
                    0L,
                    0
            );
        }

        UUID requestedInstitution = normalizedCriteria.getInstitutionId();
        if (requestedInstitution != null) {
            if (!requesterInstitutions.contains(requestedInstitution)) {
                throw forbidden();
            }
            return fetchPage(normalizedCriteria.toBuilder().scopedInstitutionIds(null).build(), effectivePageable);
        }

        return fetchPage(
                normalizedCriteria.toBuilder().scopedInstitutionIds(requesterInstitutions).build(),
                effectivePageable
        );
    }

    private PageResponse<UserResponse> fetchPage(UserSearchCriteria criteria, Pageable pageable) {
        Page<UserAccount> page = userRepository.findAll(criteria, pageable);
        List<UserResponse> items = page.getContent().stream()
                .map(userMapper::toResponse)
                .toList();

        return new PageResponse<>(
                items,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private boolean hasRole(RequesterContext requester, Role role) {
        return requester.getRoles() != null && requester.getRoles().contains(role);
    }

    private ApiException forbidden() {
        return new ApiException(HttpStatus.FORBIDDEN, "auth.forbidden", "Forbidden");
    }
}
