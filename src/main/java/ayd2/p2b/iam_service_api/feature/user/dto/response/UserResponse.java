package ayd2.p2b.iam_service_api.feature.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@Schema(description = "User profile response")
public class UserResponse {

    @Schema(format = "uuid", example = "11111111-1111-1111-1111-111111111111")
    private UUID id;
    @Schema(format = "email", example = "participant@domain.com")
    private String email;
    @Schema(example = "Jane Doe")
    private String fullName;
    @Schema(example = "Code n Bugs")
    private String organization;
    @Schema(example = "555-0101")
    private String phone;
    @Schema(example = "A123B")
    private String personalId;
    @Schema(example = "https://cdn.domain.com/photo.png")
    private String photoUrl;
    @Schema(example = "true")
    private Boolean active;
    @Schema(description = "Assigned roles", example = "[\"PARTICIPANT\"]")
    private Set<String> roles;
}

