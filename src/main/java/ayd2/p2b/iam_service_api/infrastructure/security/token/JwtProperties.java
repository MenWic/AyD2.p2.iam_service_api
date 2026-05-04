package ayd2.p2b.iam_service_api.infrastructure.security.token;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    @NotBlank
    private String secret;

    @Min(1)
    private long expirationMinutes;

    @Min(1)
    private long refreshExpirationDays;
}

