package ayd2.p2b.iam_service_api.feature.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Register user payload")
public class RegisterUserRequest {

    @NotBlank
    @Email
    @Schema(description = "User email", example = "participant@domain.com", required = true)
    private String email;

    @NotBlank
    @Size(min = 8, max = 128)
    @Schema(description = "User password", minLength = 8, maxLength = 128, required = true)
    private String password;

    @NotBlank
    @Schema(description = "Full name of the user", example = "Jane Doe", required = true)
    private String fullName;

    @NotBlank
    @Schema(description = "Organization or affiliation of the user", example = "Code n Bugs", required = true)
    private String organization;

    @NotBlank
    @Schema(description = "Phone number", example = "555-0101", required = true)
    private String phone;

    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9]+$")
    @Schema(description = "Personal identifier (ID)", example = "A123B", required = true)
    private String personalId;

    @Schema(description = "Photo URL (optional)")
    private String photoUrl;
}



