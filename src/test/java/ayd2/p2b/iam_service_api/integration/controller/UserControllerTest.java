package ayd2.p2b.iam_service_api.integration.controller;

import ayd2.p2b.iam_service_api.common.response.PageResponse;
import ayd2.p2b.iam_service_api.feature.auth.application.port.TokenParserPort;
import ayd2.p2b.iam_service_api.feature.auth.infrastructure.security.principal.AuthenticatedUser;
import ayd2.p2b.iam_service_api.feature.auth.application.register.RegisterParticipantUseCase;
import ayd2.p2b.iam_service_api.feature.user.application.activate.ActivateUserUseCase;
import ayd2.p2b.iam_service_api.feature.user.application.current.GetCurrentUserUseCase;
import ayd2.p2b.iam_service_api.feature.user.application.deactivate.DeactivateUserUseCase;
import ayd2.p2b.iam_service_api.feature.user.application.get.GetUserByIdUseCase;
import ayd2.p2b.iam_service_api.feature.user.application.list.ListUsersUseCase;
import ayd2.p2b.iam_service_api.feature.user.dto.internal.RequesterContext;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    @MockitoBean private GetUserByIdUseCase getUserByIdUseCase;
    @MockitoBean private ListUsersUseCase listUsersUseCase;
    @MockitoBean private ActivateUserUseCase activateUserUseCase;
    @MockitoBean private DeactivateUserUseCase deactivateUserUseCase;
    @MockitoBean private TokenParserPort tokenParserPort;

    @Test
    void should_return_201_api_response_when_registering_with_valid_payload() throws Exception {
        UUID userId = UUID.randomUUID();
        String payload = """
                {
                    "email": "participant@domain.com",
                    "password": "MyStrongPassword123",
                    "fullName": "Participant User",
                    "organization": "Code n Bugs",
                    "phone": "555-0101",
                    "personalId": "A123B",
                    "photoUrl": "https://cdn.domain.com/photo.png"
                }
                """;

        when(registerParticipantUseCase.execute(any())).thenReturn(
                ayd2.p2b.iam_service_api.feature.auth.dto.response.AuthResponse.builder()
                        .accessToken("access-token")
                        .refreshToken("refresh-token")
                        .user(userResponse(userId))
                        .build()
        );

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.user.id").value(userId.toString()));
    }

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
        UsernamePasswordAuthenticationToken auth = auth(userId, Set.of(Role.PARTICIPANT));

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
                .andExpect(jsonPath("$.data.id").value(userId.toString()))
                .andExpect(jsonPath("$.data.email").value("participant@domain.com"));
    }

    @Test
    void should_return_200_api_response_when_getting_user_by_id() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth = auth(requesterId, Set.of(Role.SYSTEM_ADMIN));
        UserResponse userResponse = userResponse(targetId);

        when(getUserByIdUseCase.execute(any(RequesterContext.class), eq(targetId))).thenReturn(userResponse);

        mockMvc.perform(get("/users/{id}", targetId)
                        .principal(auth)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(targetId.toString()))
                .andExpect(jsonPath("$.data.email").value("participant@domain.com"));
    }

    @Test
    void should_return_200_api_response_page_when_listing_users() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID listedUserId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth = auth(requesterId, Set.of(Role.SYSTEM_ADMIN));
        PageResponse<UserResponse> pageResponse = new PageResponse<>(
                List.of(userResponse(listedUserId)),
                0,
                20,
                1L,
                1
        );

        when(listUsersUseCase.execute(any(RequesterContext.class), any(), any())).thenReturn(pageResponse);

        mockMvc.perform(get("/users")
                        .param("role", "PARTICIPANT")
                        .param("active", "true")
                        .param("search", "participant")
                        .principal(auth)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value(listedUserId.toString()))
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1));
    }

    @Test
    void should_return_200_api_response_when_activating_user() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth = auth(requesterId, Set.of(Role.SYSTEM_ADMIN));

        when(activateUserUseCase.execute(any(RequesterContext.class), eq(targetId))).thenReturn(userResponse(targetId));

        mockMvc.perform(patch("/users/{id}/activate", targetId)
                        .principal(auth)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(targetId.toString()))
                .andExpect(jsonPath("$.data.email").value("participant@domain.com"));
    }

    @Test
    void should_return_200_api_response_when_deactivating_user() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth = auth(requesterId, Set.of(Role.SYSTEM_ADMIN));

        when(deactivateUserUseCase.execute(any(RequesterContext.class), eq(targetId))).thenReturn(userResponse(targetId));

        mockMvc.perform(patch("/users/{id}/deactivate", targetId)
                        .principal(auth)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(targetId.toString()))
                .andExpect(jsonPath("$.data.email").value("participant@domain.com"));
    }

    private UsernamePasswordAuthenticationToken auth(UUID userId, Set<Role> roles) {
        AuthenticatedUser principal = new AuthenticatedUser(userId, "participant@domain.com", roles);
        return new UsernamePasswordAuthenticationToken(principal, null, List.of());
    }

    private UserResponse userResponse(UUID userId) {
        return UserResponse.builder()
                .id(userId)
                .email("participant@domain.com")
                .fullName("Participant User")
                .organization("Code n Bugs")
                .phone("555-0101")
                .personalId("A123B")
                .active(true)
                .roles(Set.of("PARTICIPANT"))
                .build();
    }
}
