package ayd2.p2b.iam_service_api.feature.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Committee eligibility result")
public class CommitteeEligibilityResponse {

    private boolean eligible;
}
