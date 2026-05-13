package ayd2.p2b.iam_service_api.feature.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Create system admin payload")
public class CreateSystemAdminRequest {

    @NotBlank
    @Email
    @Schema(example = "systemadmin@domain.com")
    private String email;

    @NotBlank
    @Size(min = 8, max = 128)
    @Schema(minLength = 8, maxLength = 128, example = "MyStrongPassword123")
    private String password;

    @NotBlank
    @Schema(example = "System Admin")
    private String fullName;

    @NotBlank
    @Schema(example = "Code n Bugs")
    private String organization;

    @NotBlank
    @Schema(example = "555-0101")
    private String phone;

    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9]+$")
    @Schema(example = "A123B")
    private String personalId;

    @Schema(example = "https://cdn.domain.com/photo.png")
    private String photoUrl;
}
