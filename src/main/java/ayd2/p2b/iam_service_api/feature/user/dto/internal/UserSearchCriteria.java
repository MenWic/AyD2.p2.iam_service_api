package ayd2.p2b.iam_service_api.feature.user.dto.internal;

import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import lombok.Builder;
import lombok.Value;

import java.util.Set;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class UserSearchCriteria {
    Role role;
    Boolean active;
    UUID institutionId;
    String search;
    Set<UUID> scopedInstitutionIds;
    UUID createdBy;
}
