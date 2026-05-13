package ayd2.p2b.iam_service_api.feature.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Logout payload")
public class LogoutRequest {

    @NotBlank
    @Schema(description = "Refresh token to invalidate", example = "eyJhbGciOiJIUzI1NiJ9.refresh.payload", requiredMode = Schema.RequiredMode.REQUIRED)
    private String refreshToken;
}

