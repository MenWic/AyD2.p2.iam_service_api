package ayd2.p2b.iam_service_api.integration.controller;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.common.response.PageResponse;
import ayd2.p2b.iam_service_api.core.security.SecurityConfig;
import ayd2.p2b.iam_service_api.feature.auth.application.login.LoginUseCase;
import ayd2.p2b.iam_service_api.feature.auth.application.logout.LogoutUseCase;
import ayd2.p2b.iam_service_api.feature.auth.application.port.ParsedToken;
import ayd2.p2b.iam_service_api.feature.auth.application.port.TokenParserPort;
import ayd2.p2b.iam_service_api.feature.auth.application.refresh.RefreshTokenUseCase;
import ayd2.p2b.iam_service_api.feature.auth.application.register.RegisterParticipantUseCase;
import ayd2.p2b.iam_service_api.feature.auth.controller.AuthController;
import ayd2.p2b.iam_service_api.feature.auth.domain.model.TokenType;
import ayd2.p2b.iam_service_api.feature.auth.dto.response.AuthResponse;
import ayd2.p2b.iam_service_api.feature.auth.dto.response.RefreshResponse;
import ayd2.p2b.iam_service_api.feature.auth.infrastructure.security.filter.JwtAuthenticationFilter;
import ayd2.p2b.iam_service_api.feature.auth.infrastructure.security.handler.RestAuthenticationEntryPoint;
import ayd2.p2b.iam_service_api.feature.auth.infrastructure.security.token.JwtProperties;
import ayd2.p2b.iam_service_api.feature.auth.infrastructure.security.token.JwtTokenAdapter;
import ayd2.p2b.iam_service_api.feature.user.application.activate.ActivateUserUseCase;
import ayd2.p2b.iam_service_api.feature.user.application.can_be_committee.CanBeCommitteeUseCase;
import ayd2.p2b.iam_service_api.feature.user.application.create_congress_admin.CreateCongressAdminUseCase;
import ayd2.p2b.iam_service_api.feature.user.application.create_guest_speaker.CreateGuestSpeakerUseCase;
import ayd2.p2b.iam_service_api.feature.user.application.create_system_admin.CreateSystemAdminUseCase;
import ayd2.p2b.iam_service_api.feature.user.application.current.GetCurrentUserUseCase;
import ayd2.p2b.iam_service_api.feature.user.application.deactivate.DeactivateUserUseCase;
import ayd2.p2b.iam_service_api.feature.user.application.get.GetUserByIdUseCase;
import ayd2.p2b.iam_service_api.feature.user.application.list.ListUsersUseCase;
import ayd2.p2b.iam_service_api.feature.user.application.update.UpdateUserUseCase;
import ayd2.p2b.iam_service_api.feature.user.controller.UserController;
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.dto.response.CommitteeEligibilityResponse;
import ayd2.p2b.iam_service_api.feature.user.dto.response.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AuthController.class, UserController.class})
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, RestAuthenticationEntryPoint.class})
class SecurityFilterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoginUseCase loginUseCase;
    @MockitoBean
    private RefreshTokenUseCase refreshTokenUseCase;
    @MockitoBean
    private LogoutUseCase logoutUseCase;
    @MockitoBean
    private RegisterParticipantUseCase registerParticipantUseCase;

    @MockitoBean
    private GetCurrentUserUseCase getCurrentUserUseCase;
    @MockitoBean
    private GetUserByIdUseCase getUserByIdUseCase;
    @MockitoBean
    private ListUsersUseCase listUsersUseCase;
    @MockitoBean
    private ActivateUserUseCase activateUserUseCase;
    @MockitoBean
    private DeactivateUserUseCase deactivateUserUseCase;
    @MockitoBean
    private CreateSystemAdminUseCase createSystemAdminUseCase;
    @MockitoBean
    private CreateCongressAdminUseCase createCongressAdminUseCase;
    @MockitoBean
    private CreateGuestSpeakerUseCase createGuestSpeakerUseCase;
    @MockitoBean
    private UpdateUserUseCase updateUserUseCase;
    @MockitoBean
    private CanBeCommitteeUseCase canBeCommitteeUseCase;

    @MockitoBean
    private TokenParserPort tokenParserPort;

    @Test
    void should_allow_public_register_without_authorization_header() throws Exception {
        UUID userId = UUID.randomUUID();
        when(registerParticipantUseCase.execute(any())).thenReturn(authResponse(userId));

        String payload = """
                {
                  "email": "participant@domain.com",
                  "password": "Password123",
                  "fullName": "Participant User",
                  "organization": "Code n Bugs",
                  "phone": "555-0101",
                  "personalId": "A123B"
                }
                """;

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.user.id").value(userId.toString()));

        verify(registerParticipantUseCase).execute(any());
    }

    @Test
    void should_allow_public_login_without_authorization_header_and_return_api_response() throws Exception {
        UUID userId = UUID.randomUUID();
        when(loginUseCase.execute(any())).thenReturn(authResponse(userId));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "participant@domain.com",
                                  "password": "Password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.user.id").value(userId.toString()));

        verify(loginUseCase).execute(any());
    }

    @Test
    void should_allow_public_refresh_without_authorization_header_and_return_api_response() throws Exception {
        when(refreshTokenUseCase.execute(any()))
                .thenReturn(RefreshResponse.builder().accessToken("new-access-token").build());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "refresh-token"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));

        verify(refreshTokenUseCase).execute(any());
    }

    @Test
    void should_return_401_for_logout_without_access_token() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "refresh-token"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("auth.token_invalid"));
    }

    @Test
    void should_return_401_when_authorization_header_does_not_use_bearer_prefix_on_protected_route() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Token valid-access-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("auth.token_invalid"));

        verify(tokenParserPort, never()).parseToken(any(), any());
    }

    @Test
    void should_return_401_and_not_open_route_when_token_contains_unknown_role() throws Exception {
        UUID requesterId = UUID.randomUUID();
        when(tokenParserPort.parseToken("unknown-role-token", TokenType.ACCESS))
                .thenReturn(parsedAccessToken(requesterId, "UNKNOWN_ROLE"));

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer unknown-role-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("auth.token_invalid"));

        verify(getCurrentUserUseCase, never()).execute(any());
    }

    @Test
    void should_allow_guest_speaker_to_access_users_me_when_authenticated() throws Exception {
        UUID userId = UUID.randomUUID();
        when(tokenParserPort.parseToken("guest-speaker-token", TokenType.ACCESS))
                .thenReturn(parsedAccessToken(userId, Role.GUEST_SPEAKER));
        when(getCurrentUserUseCase.execute(userId))
                .thenReturn(userResponse(userId, Set.of("GUEST_SPEAKER")));

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer guest-speaker-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(userId.toString()))
                .andExpect(jsonPath("$.data.roles[0]").value("GUEST_SPEAKER"));

        verify(getCurrentUserUseCase).execute(userId);
    }

    @Test
    void should_allow_guest_speaker_to_reach_update_own_profile_when_authenticated() throws Exception {
        UUID userId = UUID.randomUUID();
        when(tokenParserPort.parseToken("guest-speaker-token", TokenType.ACCESS))
                .thenReturn(parsedAccessToken(userId, Role.GUEST_SPEAKER));
        when(updateUserUseCase.execute(any(), eq(userId), any()))
                .thenReturn(userResponse(userId, Set.of("GUEST_SPEAKER")));

        mockMvc.perform(put("/users/{id}", userId)
                        .header("Authorization", "Bearer guest-speaker-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Guest Speaker Updated",
                                  "organization": "Conference Org",
                                  "phone": "555-7777",
                                  "photoUrl": "https://cdn.domain.com/guest.png"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(userId.toString()))
                .andExpect(jsonPath("$.data.roles[0]").value("GUEST_SPEAKER"));

        verify(updateUserUseCase).execute(any(), eq(userId), any());
    }

    @Test
    void should_return_403_for_list_users_with_guest_speaker_token() throws Exception {
        UUID requesterId = UUID.randomUUID();
        when(tokenParserPort.parseToken("guest-speaker-token", TokenType.ACCESS))
                .thenReturn(parsedAccessToken(requesterId, Role.GUEST_SPEAKER));

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer guest-speaker-token"))
                .andExpect(status().isForbidden());

        verify(listUsersUseCase, never()).execute(any(), any(), any());
    }

    @Test
    void should_return_403_for_create_guest_speaker_with_guest_speaker_token() throws Exception {
        UUID requesterId = UUID.randomUUID();
        when(tokenParserPort.parseToken("guest-speaker-token", TokenType.ACCESS))
                .thenReturn(parsedAccessToken(requesterId, Role.GUEST_SPEAKER));

        mockMvc.perform(post("/users/guest-speakers")
                        .header("Authorization", "Bearer guest-speaker-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        verify(createGuestSpeakerUseCase, never()).execute(any(), any());
    }

    @Test
    void should_return_403_for_can_be_committee_with_guest_speaker_token() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        when(tokenParserPort.parseToken("guest-speaker-token", TokenType.ACCESS))
                .thenReturn(parsedAccessToken(requesterId, Role.GUEST_SPEAKER));

        mockMvc.perform(get("/users/{id}/can-be-committee", targetId)
                        .header("Authorization", "Bearer guest-speaker-token"))
                .andExpect(status().isForbidden());

        verify(canBeCommitteeUseCase, never()).execute(any(), any());
    }

    private ParsedToken parsedAccessToken(UUID userId, String roleName) {
        return ParsedToken.builder()
                .userId(userId)
                .subject(userId.toString())
                .email("user@domain.com")
                .roles(List.of(roleName))
                .tokenType(TokenType.ACCESS)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    @Test
    void should_reach_logout_use_case_with_valid_access_token_and_return_204() throws Exception {
        UUID userId = UUID.randomUUID();
        when(tokenParserPort.parseToken("valid-access-token", TokenType.ACCESS))
                .thenReturn(parsedAccessToken(userId, Role.PARTICIPANT));

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer valid-access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "refresh-token"
                                }
                                """))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(logoutUseCase).execute(eq(userId), any());
    }

    @Test
    void should_return_401_for_users_me_without_access_token() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("auth.token_invalid"));
    }

    @Test
    void should_reach_users_me_with_valid_access_token_and_return_api_response() throws Exception {
        UUID userId = UUID.randomUUID();
        when(tokenParserPort.parseToken("valid-access-token", TokenType.ACCESS))
                .thenReturn(parsedAccessToken(userId, Role.PARTICIPANT));
        when(getCurrentUserUseCase.execute(userId)).thenReturn(userResponse(userId, Set.of("PARTICIPANT")));

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer valid-access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(userId.toString()))
                .andExpect(jsonPath("$.data.roles[0]").value("PARTICIPANT"));

        verify(getCurrentUserUseCase).execute(userId);
    }

    @Test
    void should_return_401_for_put_users_id_without_access_token() throws Exception {
        mockMvc.perform(put("/users/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Updated Name",
                                  "organization": "Updated Org",
                                  "phone": "555-9999",
                                  "photoUrl": "https://cdn.domain.com/new-photo.png"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("auth.token_invalid"));
    }

    @Test
    void should_return_401_for_can_be_committee_without_access_token() throws Exception {
        mockMvc.perform(get("/users/{id}/can-be-committee", UUID.randomUUID()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("auth.token_invalid"));
    }

    @Test
    void should_reach_can_be_committee_with_valid_congress_admin_token() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        when(tokenParserPort.parseToken("congress-admin-token", TokenType.ACCESS))
                .thenReturn(parsedAccessToken(requesterId, Role.CONGRESS_ADMIN));
        when(canBeCommitteeUseCase.execute(any(), eq(targetId)))
                .thenReturn(CommitteeEligibilityResponse.builder().eligible(true).build());

        mockMvc.perform(get("/users/{id}/can-be-committee", targetId)
                        .header("Authorization", "Bearer congress-admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eligible").value(true));

        verify(canBeCommitteeUseCase).execute(any(), eq(targetId));
    }

    @Test
    void should_return_401_for_invalid_access_token_on_protected_route() throws Exception {
        when(tokenParserPort.parseToken("invalid-token", TokenType.ACCESS))
                .thenThrow(new ApiException(HttpStatus.UNAUTHORIZED, "auth.token_invalid",
                        "Invalid token"));

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("auth.token_invalid"));
    }

    @Test
    void should_not_open_unknown_route_publicly() throws Exception {
        mockMvc.perform(get("/unknown"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("auth.token_invalid"));
    }

    @Test
    void should_return_page_response_shape_for_users_list() throws Exception {
        UUID requesterId = UUID.randomUUID();
        when(tokenParserPort.parseToken("system-admin-token", TokenType.ACCESS))
                .thenReturn(parsedAccessToken(requesterId, Role.SYSTEM_ADMIN));
        when(listUsersUseCase.execute(any(), any(), any()))
                .thenReturn(PageResponse.<UserResponse>builder()
                        .items(List.of(userResponse(UUID.randomUUID(), Set.of("PARTICIPANT"))))
                        .page(0)
                        .size(20)
                        .totalItems(1)
                        .totalPages(1)
                        .build());

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer system-admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1));
    }

    @Test
    void should_return_validation_failed_problem_detail_for_invalid_request_body() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation.failed"))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void should_return_stable_problem_detail_code_for_use_case_forbidden_error() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        when(tokenParserPort.parseToken("forbidden-token", TokenType.ACCESS))
                .thenReturn(parsedAccessToken(requesterId, Role.CONGRESS_ADMIN));
        when(canBeCommitteeUseCase.execute(any(), eq(targetId)))
                .thenThrow(new ApiException(HttpStatus.FORBIDDEN, "auth.forbidden", "Forbidden"));

        mockMvc.perform(get("/users/{id}/can-be-committee", targetId)
                        .header("Authorization", "Bearer forbidden-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("auth.forbidden"));
    }

    @Test
    void should_return_403_for_create_system_admin_with_congress_admin_token() throws Exception {
        UUID requesterId = UUID.randomUUID();
        when(tokenParserPort.parseToken("congress-admin-token", TokenType.ACCESS))
                .thenReturn(parsedAccessToken(requesterId, Role.CONGRESS_ADMIN));

        mockMvc.perform(post("/users/system-admins")
                        .header("Authorization", "Bearer congress-admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void should_return_403_for_create_congress_admin_with_congress_admin_token() throws Exception {
        UUID requesterId = UUID.randomUUID();
        when(tokenParserPort.parseToken("congress-admin-token", TokenType.ACCESS))
                .thenReturn(parsedAccessToken(requesterId, Role.CONGRESS_ADMIN));

        mockMvc.perform(post("/users/congress-admins")
                        .header("Authorization", "Bearer congress-admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void should_return_403_for_create_guest_speaker_with_system_admin_token() throws Exception {
        UUID requesterId = UUID.randomUUID();
        when(tokenParserPort.parseToken("system-admin-token", TokenType.ACCESS))
                .thenReturn(parsedAccessToken(requesterId, Role.SYSTEM_ADMIN));

        mockMvc.perform(post("/users/guest-speakers")
                        .header("Authorization", "Bearer system-admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void should_return_403_for_activate_without_system_admin() throws Exception {
        UUID requesterId = UUID.randomUUID();
        when(tokenParserPort.parseToken("participant-token", TokenType.ACCESS))
                .thenReturn(parsedAccessToken(requesterId, Role.PARTICIPANT));

        mockMvc.perform(patch("/users/{id}/activate", UUID.randomUUID())
                        .header("Authorization", "Bearer participant-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void should_return_403_for_list_users_with_participant_token() throws Exception {
        UUID requesterId = UUID.randomUUID();
        when(tokenParserPort.parseToken("participant-token", TokenType.ACCESS))
                .thenReturn(parsedAccessToken(requesterId, Role.PARTICIPANT));

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer participant-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void should_return_403_for_can_be_committee_with_system_admin_token() throws Exception {
        UUID requesterId = UUID.randomUUID();
        when(tokenParserPort.parseToken("system-admin-token", TokenType.ACCESS))
                .thenReturn(parsedAccessToken(requesterId, Role.SYSTEM_ADMIN));

        mockMvc.perform(get("/users/{id}/can-be-committee", UUID.randomUUID())
                        .header("Authorization", "Bearer system-admin-token"))
                .andExpect(status().isForbidden());
    }


    private ParsedToken parsedAccessToken(UUID userId, Role role) {
        return ParsedToken.builder()
                .userId(userId)
                .subject(userId.toString())
                .email("user@domain.com")
                .roles(List.of(role.name()))
                .tokenType(TokenType.ACCESS)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    private AuthResponse authResponse(UUID userId) {
        return AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .user(userResponse(userId, Set.of("PARTICIPANT")))
                .build();
    }

    private UserResponse userResponse(UUID userId, Set<String> roles) {
        return UserResponse.builder()
                .id(userId)
                .email("participant@domain.com")
                .fullName("Participant User")
                .organization("Code n Bugs")
                .phone("555-0101")
                .personalId("A123B")
                .active(true)
                .roles(roles)
                .build();
    }
}
