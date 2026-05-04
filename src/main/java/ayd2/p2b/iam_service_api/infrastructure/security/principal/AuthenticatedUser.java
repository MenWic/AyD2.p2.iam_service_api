package ayd2.p2b.iam_service_api.infrastructure.security.principal;

import ayd2.p2b.iam_service_api.domain.model.user.Role;

import java.util.Set;
import java.util.UUID;

public record AuthenticatedUser(UUID userId, String email, Set<Role> roles) {
}

