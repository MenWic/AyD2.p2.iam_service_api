package ayd2.p2b.iam_service_api.feature.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Update user profile payload")
public class UpdateUserRequest {

    @NotBlank
    @Schema(example = "Jane Doe")
    private String fullName;

    @NotBlank
    @Schema(example = "Code n Bugs")
    private String organization;

    @NotBlank
    @Schema(example = "555-0101")
    private String phone;

    @Schema(example = "https://cdn.domain.com/photo.png")
    private String photoUrl;
}
