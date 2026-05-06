package ayd2.p2b.iam_service_api.feature.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Refresh response")
public class RefreshResponse {
    private String accessToken;
}

