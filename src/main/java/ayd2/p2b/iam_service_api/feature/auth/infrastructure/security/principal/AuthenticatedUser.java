package ayd2.p2b.iam_service_api.feature.auth.infrastructure.security.principal;

import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;

import java.util.Set;
import java.util.UUID;

public record AuthenticatedUser(UUID userId, String email, Set<Role> roles) {
}

