package ayd2.p2b.iam_service_api.integration.controller;

import ayd2.p2b.iam_service_api.feature.auth.application.port.TokenParserPort;
import ayd2.p2b.iam_service_api.feature.auth.infrastructure.security.principal.AuthenticatedUser;
import ayd2.p2b.iam_service_api.feature.auth.application.register.RegisterParticipantUseCase;
import ayd2.p2b.iam_service_api.feature.user.application.current.GetCurrentUserUseCase;
import ayd2.p2b.iam_service_api.feature.user.dto.response.UserResponse;
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.controller.UserController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private GetCurrentUserUseCase getCurrentUserUseCase;
    @MockitoBean private RegisterParticipantUseCase registerParticipantUseCase;
    @MockitoBean private TokenParserPort tokenParserPort;

    @Test
    void should_return_400_when_registering_with_missing_organization() throws Exception {
        String invalidPayload = """
                {
                    "email": "test@domain.com",
                    "password": "Password123",
                    "fullName": "Jane Doe",
                    "phone": "555-0101",
                    "personalId": "A123B"
                }
                """;

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_400_when_registering_with_missing_phone() throws Exception {
        String invalidPayload = """
                {
                    "email": "test@domain.com",
                    "password": "Password123",
                    "fullName": "Jane Doe",
                    "organization": "Code n Bugs",
                    "personalId": "A123B"
                }
                """;

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_400_when_registering_with_missing_personalId() throws Exception {
        String invalidPayload = """
                {
                    "email": "test@domain.com",
                    "password": "Password123",
                    "fullName": "Jane Doe",
                    "organization": "Code n Bugs",
                    "phone": "555-0101"
                }
                """;

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_400_when_registering_with_short_password() throws Exception {
        String invalidPayload = """
                {
                    "email": "test@domain.com",
                    "password": "short",
                    "fullName": "Jane Doe",
                    "organization": "Code n Bugs",
                    "phone": "555-0101",
                    "personalId": "A123B"
                }
                """;

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_current_user_when_authenticated_request_is_sent() throws Exception {
        UUID userId = UUID.randomUUID();
        AuthenticatedUser principal = new AuthenticatedUser(userId, "participant@domain.com", Set.of(Role.PARTICIPANT));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());

        UserResponse userResponse = UserResponse.builder()
                .id(userId)
                .email("participant@domain.com")
                .fullName("Participant User")
                .organization("Code n Bugs")
                .phone("555-0101")
                .personalId("A123B")
                .active(true)
                .roles(Set.of("PARTICIPANT"))
                .build();
        when(getCurrentUserUseCase.execute(userId)).thenReturn(userResponse);

        mockMvc.perform(get("/users/me")
                        .principal(auth)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("participant@domain.com"));
    }
}
