package ayd2.p2b.iam_service_api.application.port.security;

public interface TokenHashPort {

    String sha256(String value);
}

