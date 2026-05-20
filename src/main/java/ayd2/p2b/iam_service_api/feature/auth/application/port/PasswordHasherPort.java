package ayd2.p2b.iam_service_api.feature.auth.application.port;

public interface PasswordHasherPort {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}
