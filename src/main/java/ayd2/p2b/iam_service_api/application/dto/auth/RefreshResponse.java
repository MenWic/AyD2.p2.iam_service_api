package ayd2.p2b.iam_service_api.application.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Refresh response")
public class RefreshResponse {

    @Schema(description = "New access token")
    private String accessToken;
}
