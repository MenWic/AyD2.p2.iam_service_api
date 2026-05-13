package ayd2.p2b.iam_service_api.feature.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Login payload")
public class LoginRequest {

    @NotBlank
    @Email
    @Schema(description = "User email", example = "user@domain.com", format = "email", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank
    @Schema(description = "User password", example = "MyStrongPassword123", minLength = 8, maxLength = 128, requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}

