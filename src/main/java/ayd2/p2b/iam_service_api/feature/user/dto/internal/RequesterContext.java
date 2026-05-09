package ayd2.p2b.iam_service_api.feature.user.dto.internal;

import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequesterContext {
    private UUID userId;
    private Set<Role> roles;
}
