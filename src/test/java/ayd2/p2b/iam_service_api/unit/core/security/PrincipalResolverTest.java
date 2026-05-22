package ayd2.p2b.iam_service_api.unit.core.security;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.core.security.PrincipalResolver;
import ayd2.p2b.iam_service_api.feature.auth.infrastructure.security.principal.AuthenticatedUser;
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.dto.internal.RequesterContext;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PrincipalResolverTest {

    @Test
    void should_throw_token_invalid_when_authentication_is_null() {
        ApiException exception = assertThrows(ApiException.class, () -> PrincipalResolver.resolve(null));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("auth.token_invalid", exception.getCode());
    }

    @Test
    void should_throw_token_invalid_when_principal_is_not_authenticated_user() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("plain-user", null);

        ApiException exception = assertThrows(ApiException.class, () -> PrincipalResolver.resolve(authentication));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("auth.token_invalid", exception.getCode());
    }

    @Test
    void should_resolve_requester_context_from_authenticated_user() {
        UUID userId = UUID.randomUUID();
        Set<Role> roles = Set.of(Role.PARTICIPANT, Role.GUEST_SPEAKER);
        AuthenticatedUser principal = new AuthenticatedUser(userId, "user@domain.com", roles);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null);

        RequesterContext requester = PrincipalResolver.resolve(authentication);

        assertEquals(userId, requester.getUserId());
        assertEquals(roles, requester.getRoles());
    }
}
