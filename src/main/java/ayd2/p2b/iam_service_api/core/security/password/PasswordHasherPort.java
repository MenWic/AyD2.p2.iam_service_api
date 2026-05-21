package ayd2.p2b.iam_service_api.core.security.password;

/**
 * Cross-cutting security port for password hashing.
 * Lives in core/security/password/ so any feature can depend on it
 * without creating cross-feature application-layer coupling.
 */
public interface PasswordHasherPort {
  String encode(String rawPassword);

  boolean matches(String rawPassword, String encodedPassword);
}
