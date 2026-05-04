package ayd2.p2b.iam_service_api.domain.model.user;

import lombok.Builder;
import lombok.Value;

import java.util.Set;
import java.util.UUID;

@Value
@Builder
public class UserAccount {
    UUID id;
    String email;
    String passwordHash;
    String fullName;
    String organization;
    String phone;
    String personalId;
    String photoUrl;
    Boolean active;
    Set<Role> roles;
}

