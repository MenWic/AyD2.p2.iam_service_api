package ayd2.p2b.iam_service_api.core.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IamOpenApiConfig {

    @Bean
    public OpenAPI iamOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("IAM Service API")
                        .description("Authentication and user management API for Code 'n Bugs Congress Management Platform.")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ));
    }
}
