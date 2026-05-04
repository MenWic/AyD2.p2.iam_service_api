package ayd2.p2b.iam_service_api.application.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Self registration request")
public class RegisterUserRequest {

    @NotBlank
    @Email
    @Schema(example = "participant@domain.com")
    private String email;

    @NotBlank
    @Size(min = 8, max = 128)
    @Schema(example = "MyStrongPassword123")
    private String password;

    @NotBlank
    @Schema(example = "Ada Lovelace")
    private String fullName;

    @NotBlank
    @Schema(example = "Code n Bugs")
    private String organization;

    @NotBlank
    @Schema(example = "555-0101")
    private String phone;

    @NotBlank
    @Schema(example = "A1234B56")
    private String personalId;

    @Schema(example = "https://cdn.domain.com/photos/user.png")
    private String photoUrl;
}
