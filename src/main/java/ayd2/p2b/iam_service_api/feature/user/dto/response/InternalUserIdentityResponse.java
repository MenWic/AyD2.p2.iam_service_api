package ayd2.p2b.iam_service_api.feature.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Internal user identity response")
public class InternalUserIdentityResponse {

    @Schema(format = "uuid", example = "11111111-1111-1111-1111-111111111111")
    private UUID id;

    @Schema(example = "A123B")
    private String personalId;
}
