package ayd2.p2b.iam_service_api.application.dto.auth;

import ayd2.p2b.iam_service_api.application.dto.user.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Authentication response")
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private UserResponse user;
}
