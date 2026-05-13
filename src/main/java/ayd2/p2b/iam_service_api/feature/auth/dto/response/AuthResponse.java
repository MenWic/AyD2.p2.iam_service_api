package ayd2.p2b.iam_service_api.feature.auth.dto.response;

import ayd2.p2b.iam_service_api.feature.user.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Authentication response")
public class AuthResponse {

    @Schema(description = "JWT access token")
    private String accessToken;
    @Schema(description = "JWT refresh token")
    private String refreshToken;
    @Schema(description = "Authenticated user profile")
    private UserResponse user;
}

