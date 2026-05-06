package ayd2.p2b.iam_service_api.feature.auth.application.port;

public interface TokenHashPort {
    String sha256(String token);
}

