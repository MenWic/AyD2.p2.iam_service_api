package ayd2.p2b.iam_service_api.feature.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Create guest speaker payload")
public class CreateGuestSpeakerRequest {

    @NotBlank
    @Email
    @Schema(example = "guestspeaker@domain.com")
    private String email;

    @NotBlank
    @Schema(example = "Guest Speaker")
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

    @Schema(description = "Optional password. If blank or missing, password hash remains null.")
    private String password;
}
