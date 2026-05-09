package ayd2.p2b.iam_service_api.integration.controller;

import ayd2.p2b.iam_service_api.feature.auth.application.login.LoginUseCase;
import ayd2.p2b.iam_service_api.feature.auth.application.logout.LogoutUseCase;
import ayd2.p2b.iam_service_api.feature.auth.application.port.TokenParserPort;
import ayd2.p2b.iam_service_api.feature.auth.application.refresh.RefreshTokenUseCase;
import ayd2.p2b.iam_service_api.feature.auth.dto.response.AuthResponse;
import ayd2.p2b.iam_service_api.feature.auth.dto.response.RefreshResponse;
import ayd2.p2b.iam_service_api.feature.auth.infrastructure.security.principal.AuthenticatedUser;
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.dto.response.UserResponse;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ayd2.p2b.iam_service_api.feature.auth.controller.AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private LoginUseCase loginUseCase;
    @MockitoBean private RefreshTokenUseCase refreshTokenUseCase;
    @MockitoBean private LogoutUseCase logoutUseCase;
    @MockitoBean private TokenParserPort tokenParserPort;

    @Test
    void should_return_200_api_response_when_login_is_successful() throws Exception {
        UUID userId = UUID.randomUUID();
        AuthResponse response = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .user(UserResponse.builder()
                        .id(userId)
                        .email("participant@domain.com")
                        .roles(Set.of("PARTICIPANT"))
                        .build())
                .build();

        when(loginUseCase.execute(any())).thenReturn(response);

        String payload = """
                {
                    "email": "participant@domain.com",
                    "password": "MyStrongPassword123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.user.id").value(userId.toString()));
    }

    @Test
    void should_return_200_api_response_when_refresh_is_successful() throws Exception {
        when(refreshTokenUseCase.execute(any())).thenReturn(
                RefreshResponse.builder().accessToken("new-access-token").build()
        );

        String payload = """
                {
                    "refreshToken": "refresh-token"
                }
                """;

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
    }

    @Test
    void should_return_204_no_content_when_logout_is_successful() throws Exception {
        UUID userId = UUID.randomUUID();
        AuthenticatedUser principal = new AuthenticatedUser(userId, "participant@domain.com", Set.of(Role.PARTICIPANT));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());

        String payload = """
                {
                    "refreshToken": "refresh-token"
                }
                """;

        mockMvc.perform(post("/auth/logout")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(logoutUseCase).execute(eq(userId), any());
    }
}
