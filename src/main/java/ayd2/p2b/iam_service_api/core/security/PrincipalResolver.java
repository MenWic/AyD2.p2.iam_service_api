package ayd2.p2b.iam_service_api.core.security;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.feature.auth.infrastructure.security.principal.AuthenticatedUser;
import ayd2.p2b.iam_service_api.feature.user.dto.internal.RequesterContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

/**
 * Cross-cutting security helper that extracts a {@link RequesterContext} from
 * the authenticated principal. Lives in core/security/ so any feature
 * controller can depend on it without creating cross-feature coupling.
 */
public final class PrincipalResolver {

  private PrincipalResolver() {
  }

  public static RequesterContext resolve(Authentication authentication) {
    if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "auth.token_invalid", "Invalid authentication");
    }
    return new RequesterContext(authenticatedUser.getUserId(), authenticatedUser.getRoles());
  }
}
