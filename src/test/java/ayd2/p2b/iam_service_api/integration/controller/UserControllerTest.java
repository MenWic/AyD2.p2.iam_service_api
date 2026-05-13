package ayd2.p2b.iam_service_api.integration.controller;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.common.response.PageResponse;
import ayd2.p2b.iam_service_api.feature.auth.application.port.TokenParserPort;
import ayd2.p2b.iam_service_api.feature.auth.infrastructure.security.principal.AuthenticatedUser;
import ayd2.p2b.iam_service_api.feature.auth.application.register.RegisterParticipantUseCase;
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
import ayd2.p2b.iam_service_api.feature.user.dto.internal.RequesterContext;
import ayd2.p2b.iam_service_api.feature.user.dto.response.CommitteeEligibilityResponse;
import ayd2.p2b.iam_service_api.feature.user.dto.response.UserResponse;
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.controller.UserController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    @MockitoBean private CreateSystemAdminUseCase createSystemAdminUseCase;
    @MockitoBean private CreateCongressAdminUseCase createCongressAdminUseCase;
    @MockitoBean private CreateGuestSpeakerUseCase createGuestSpeakerUseCase;
    @MockitoBean private UpdateUserUseCase updateUserUseCase;
    @MockitoBean private CanBeCommitteeUseCase canBeCommitteeUseCase;
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
    void should_return_400_validation_failed_when_registering_with_invalid_personal_id_format() throws Exception {
        String invalidPayload = """
                {
                    "email": "test@domain.com",
                    "password": "Password123",
                    "fullName": "Jane Doe",
                    "organization": "Code n Bugs",
                    "phone": "555-0101",
                    "personalId": "A-123"
                }
                """;

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation.failed"));
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

    @Test
    void should_return_201_with_api_response_when_creating_system_admin() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID createdId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth = auth(requesterId, Set.of(Role.SYSTEM_ADMIN));

        when(createSystemAdminUseCase.execute(any(RequesterContext.class), any())).thenReturn(userResponse(createdId));

        String payload = """
                {
                    "email": "systemadmin@domain.com",
                    "password": "Password123",
                    "fullName": "System Admin",
                    "organization": "Code n Bugs",
                    "phone": "555-0101",
                    "personalId": "A123B"
                }
                """;

        mockMvc.perform(post("/users/system-admins")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(createdId.toString()))
                .andExpect(jsonPath("$.data.email").value("participant@domain.com"));
    }

    @Test
    void should_return_201_with_api_response_when_creating_congress_admin() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID createdId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth = auth(requesterId, Set.of(Role.SYSTEM_ADMIN));
        UUID institutionId = UUID.randomUUID();

        when(createCongressAdminUseCase.execute(any(RequesterContext.class), any())).thenReturn(userResponse(createdId));

        String payload = """
                {
                    "email": "congressadmin@domain.com",
                    "password": "Password123",
                    "fullName": "Congress Admin",
                    "organization": "Code n Bugs",
                    "phone": "555-0101",
                    "personalId": "A123B",
                    "institutionIds": ["%s"]
                }
                """.formatted(institutionId);

        mockMvc.perform(post("/users/congress-admins")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(createdId.toString()))
                .andExpect(jsonPath("$.data.email").value("participant@domain.com"));
    }

    @Test
    void should_return_201_with_api_response_when_creating_guest_speaker_without_password() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID createdId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth = auth(requesterId, Set.of(Role.CONGRESS_ADMIN));

        when(createGuestSpeakerUseCase.execute(any(RequesterContext.class), any())).thenReturn(userResponse(createdId));

        String payload = """
                {
                    "email": "guestspeaker@domain.com",
                    "fullName": "Guest Speaker",
                    "organization": "Code n Bugs",
                    "phone": "555-0101",
                    "personalId": "A123B"
                }
                """;

        mockMvc.perform(post("/users/guest-speakers")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(createdId.toString()))
                .andExpect(jsonPath("$.data.email").value("participant@domain.com"));
    }

    @Test
    void should_return_400_when_creating_congress_admin_with_empty_institution_ids() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth = auth(requesterId, Set.of(Role.SYSTEM_ADMIN));

        String payload = """
                {
                    "email": "congressadmin@domain.com",
                    "password": "Password123",
                    "fullName": "Congress Admin",
                    "organization": "Code n Bugs",
                    "phone": "555-0101",
                    "personalId": "A123B",
                    "institutionIds": []
                }
                """;

        mockMvc.perform(post("/users/congress-admins")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_400_when_creating_congress_admin_with_null_institution_id() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth = auth(requesterId, Set.of(Role.SYSTEM_ADMIN));

        String payload = """
                {
                    "email": "congressadmin@domain.com",
                    "password": "Password123",
                    "fullName": "Congress Admin",
                    "organization": "Code n Bugs",
                    "phone": "555-0101",
                    "personalId": "A123B",
                    "institutionIds": [null]
                }
                """;

        mockMvc.perform(post("/users/congress-admins")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_400_when_guest_speaker_password_is_nonblank_and_too_short() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth = auth(requesterId, Set.of(Role.CONGRESS_ADMIN));

        when(createGuestSpeakerUseCase.execute(any(RequesterContext.class), any()))
                .thenThrow(new ApiException(HttpStatus.BAD_REQUEST, "validation.failed", "Password length must be between 8 and 128"));

        String payload = """
                {
                    "email": "guestspeaker@domain.com",
                    "fullName": "Guest Speaker",
                    "organization": "Code n Bugs",
                    "phone": "555-0101",
                    "personalId": "A123B",
                    "password": "short"
                }
                """;

        mockMvc.perform(post("/users/guest-speakers")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_400_when_personal_id_has_invalid_format_for_system_admin_creation() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth = auth(requesterId, Set.of(Role.SYSTEM_ADMIN));

        String payload = """
                {
                    "email": "systemadmin@domain.com",
                    "password": "Password123",
                    "fullName": "System Admin",
                    "organization": "Code n Bugs",
                    "phone": "555-0101",
                    "personalId": "A-123"
                }
                """;

        mockMvc.perform(post("/users/system-admins")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_403_when_creating_system_admin_without_permission() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth = auth(requesterId, Set.of(Role.PARTICIPANT));

        when(createSystemAdminUseCase.execute(any(RequesterContext.class), any()))
                .thenThrow(new ApiException(HttpStatus.FORBIDDEN, "auth.forbidden", "Forbidden"));

        String payload = """
                {
                    "email": "systemadmin@domain.com",
                    "password": "Password123",
                    "fullName": "System Admin",
                    "organization": "Code n Bugs",
                    "phone": "555-0101",
                    "personalId": "A123B"
                }
                """;

        mockMvc.perform(post("/users/system-admins")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    void should_return_200_with_api_response_when_updating_profile() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth = auth(requesterId, Set.of(Role.SYSTEM_ADMIN));

        when(updateUserUseCase.execute(any(RequesterContext.class), eq(targetId), any())).thenReturn(userResponse(targetId));

        String payload = """
                {
                    "fullName": "Updated Name",
                    "organization": "Updated Org",
                    "phone": "555-9999",
                    "photoUrl": "https://cdn.domain.com/new-photo.png"
                }
                """;

        mockMvc.perform(put("/users/{id}", targetId)
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(targetId.toString()))
                .andExpect(jsonPath("$.data.email").value("participant@domain.com"));
    }

    @Test
    void should_return_403_when_updating_profile_without_permission() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth = auth(requesterId, Set.of(Role.PARTICIPANT));

        when(updateUserUseCase.execute(any(RequesterContext.class), eq(targetId), any()))
                .thenThrow(new ApiException(HttpStatus.FORBIDDEN, "auth.forbidden", "Forbidden"));

        String payload = """
                {
                    "fullName": "Updated Name",
                    "organization": "Updated Org",
                    "phone": "555-9999",
                    "photoUrl": "https://cdn.domain.com/new-photo.png"
                }
                """;

        mockMvc.perform(put("/users/{id}", targetId)
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    void should_return_400_when_updating_profile_with_blank_required_fields() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth = auth(requesterId, Set.of(Role.PARTICIPANT));

        String payload = """
                {
                    "fullName": " ",
                    "organization": " ",
                    "phone": " ",
                    "photoUrl": "https://cdn.domain.com/new-photo.png"
                }
                """;

        mockMvc.perform(put("/users/{id}", targetId)
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_200_when_updating_profile_and_preserving_identity_security_fields() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth = auth(requesterId, Set.of(Role.SYSTEM_ADMIN));
        UserResponse response = UserResponse.builder()
                .id(targetId)
                .email("participant@domain.com")
                .fullName("Updated Name")
                .organization("Updated Org")
                .phone("555-9999")
                .personalId("A123B")
                .active(true)
                .roles(Set.of("PARTICIPANT"))
                .build();

        when(updateUserUseCase.execute(any(RequesterContext.class), eq(targetId), any())).thenReturn(response);

        String payload = """
                {
                    "fullName": "Updated Name",
                    "organization": "Updated Org",
                    "phone": "555-9999",
                    "photoUrl": "https://cdn.domain.com/new-photo.png"
                }
                """;

        mockMvc.perform(put("/users/{id}", targetId)
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("participant@domain.com"))
                .andExpect(jsonPath("$.data.personalId").value("A123B"))
                .andExpect(jsonPath("$.data.active").value(true))
                .andExpect(jsonPath("$.data.roles[0]").value("PARTICIPANT"));
    }

    @Test
    void should_return_200_with_api_response_when_checking_committee_eligibility() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth = auth(requesterId, Set.of(Role.CONGRESS_ADMIN));

        when(canBeCommitteeUseCase.execute(any(RequesterContext.class), eq(targetId)))
                .thenReturn(committeeEligibility(true));

        mockMvc.perform(get("/users/{id}/can-be-committee", targetId)
                        .principal(auth)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eligible").value(true));
    }

    @Test
    void should_return_200_eligible_false_for_guest_speaker_only_in_committee_validation() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth = auth(requesterId, Set.of(Role.CONGRESS_ADMIN));

        when(canBeCommitteeUseCase.execute(any(RequesterContext.class), eq(targetId)))
                .thenReturn(committeeEligibility(false));

        mockMvc.perform(get("/users/{id}/can-be-committee", targetId)
                        .principal(auth)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eligible").value(false));
    }

    @Test
    void should_return_200_eligible_false_for_missing_user_in_committee_validation() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth = auth(requesterId, Set.of(Role.CONGRESS_ADMIN));

        when(canBeCommitteeUseCase.execute(any(RequesterContext.class), eq(targetId)))
                .thenReturn(committeeEligibility(false));

        mockMvc.perform(get("/users/{id}/can-be-committee", targetId)
                        .principal(auth)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eligible").value(false));
    }

    @Test
    void should_return_403_for_unauthorized_role_in_committee_validation() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth = auth(requesterId, Set.of(Role.PARTICIPANT));

        when(canBeCommitteeUseCase.execute(any(RequesterContext.class), eq(targetId)))
                .thenThrow(new ApiException(HttpStatus.FORBIDDEN, "auth.forbidden", "Forbidden"));

        mockMvc.perform(get("/users/{id}/can-be-committee", targetId)
                        .principal(auth)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
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

    private CommitteeEligibilityResponse committeeEligibility(boolean eligible) {
        return CommitteeEligibilityResponse.builder()
                .eligible(eligible)
                .build();
    }
}
