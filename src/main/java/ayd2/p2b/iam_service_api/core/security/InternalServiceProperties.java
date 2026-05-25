package ayd2.p2b.iam_service_api.core.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "security.internal")
public class InternalServiceProperties {

    private String serviceToken;
}
