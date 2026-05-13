package ayd2.p2b.iam_service_api.feature.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
@Schema(description = "Create congress admin payload")
public class CreateCongressAdminRequest {

    @NotBlank
    @Email
    @Schema(example = "congressadmin@domain.com", format = "email", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank
    @Size(min = 8, max = 128)
    @Schema(minLength = 8, maxLength = 128, example = "MyStrongPassword123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank
    @Schema(example = "Congress Admin")
    private String fullName;

    @NotBlank
    @Schema(example = "Code n Bugs")
    private String organization;

    @NotBlank
    @Schema(example = "555-0101")
    private String phone;

    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9]+$")
    @Schema(example = "A123B", pattern = "^[A-Za-z0-9]+$", requiredMode = Schema.RequiredMode.REQUIRED)
    private String personalId;

    @Schema(example = "https://cdn.domain.com/photo.png")
    private String photoUrl;

    @NotEmpty
    @Schema(
            description = "Linked institution IDs",
            example = "[\"22222222-2222-2222-2222-222222222222\"]",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Set<@NotNull UUID> institutionIds;
}
