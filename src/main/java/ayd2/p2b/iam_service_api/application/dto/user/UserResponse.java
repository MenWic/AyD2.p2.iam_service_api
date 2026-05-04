package ayd2.p2b.iam_service_api.application.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@Schema(description = "User profile response")
public class UserResponse {

    private UUID id;
    private String email;
    private String fullName;
    private String organization;
    private String phone;
    private String personalId;
    private String photoUrl;
    private Boolean active;
    private Set<String> roles;
}
