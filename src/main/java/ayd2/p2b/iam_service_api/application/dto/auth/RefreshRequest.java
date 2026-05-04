package ayd2.p2b.iam_service_api.application.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Refresh token payload")
public class RefreshRequest {

    @NotBlank
    @Schema(description = "Refresh token")
    private String refreshToken;
}
