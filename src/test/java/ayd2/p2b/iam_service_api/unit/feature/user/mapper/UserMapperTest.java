package ayd2.p2b.iam_service_api.unit.feature.user.mapper;

import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import ayd2.p2b.iam_service_api.feature.user.dto.response.UserResponse;
import ayd2.p2b.iam_service_api.feature.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void should_return_empty_roles_when_source_roles_are_null() {
        UserAccount account = UserAccount.builder()
                .id(UUID.randomUUID())
                .email("user@domain.com")
                .roles(null)
                .build();

        UserResponse response = mapper.toResponse(account);

        assertNotNull(response.getRoles());
        assertTrue(response.getRoles().isEmpty());
    }

    @Test
    void should_map_role_names_when_source_roles_exist() {
        UserAccount account = UserAccount.builder()
                .id(UUID.randomUUID())
                .email("user@domain.com")
                .roles(Set.of(Role.SYSTEM_ADMIN, Role.PARTICIPANT))
                .build();

        UserResponse response = mapper.toResponse(account);

        assertEquals(Set.of("SYSTEM_ADMIN", "PARTICIPANT"), response.getRoles());
    }
}
