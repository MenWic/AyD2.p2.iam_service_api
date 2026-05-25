package ayd2.p2b.iam_service_api.integration.controller;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.core.security.InternalServiceTokenValidator;
import ayd2.p2b.iam_service_api.core.security.SecurityConfig;
import ayd2.p2b.iam_service_api.feature.auth.application.port.TokenParserPort;
import ayd2.p2b.iam_service_api.feature.auth.infrastructure.security.filter.JwtAuthenticationFilter;
import ayd2.p2b.iam_service_api.feature.auth.infrastructure.security.handler.RestAuthenticationEntryPoint;
import ayd2.p2b.iam_service_api.feature.user.application.find_by_personal_id.FindUserByPersonalIdUseCase;
import ayd2.p2b.iam_service_api.feature.user.controller.InternalUserController;
import ayd2.p2b.iam_service_api.feature.user.dto.response.InternalUserIdentityResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalUserController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, RestAuthenticationEntryPoint.class, InternalServiceTokenValidator.class})
@TestPropertySource(properties = {
        "security.jwt.secret=test-jwt-secret-value-with-at-least-32-chars",
        "security.jwt.expiration-minutes=15",
        "security.jwt.refresh-expiration-days=7",
        "security.internal.service-token=conference-internal-token"
})
class InternalUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FindUserByPersonalIdUseCase findUserByPersonalIdUseCase;

    @MockitoBean
    private TokenParserPort tokenParserPort;

    @Test
    void should_return_200_with_api_response_when_service_token_is_valid_and_no_bearer_jwt_is_present() throws Exception {
        UUID userId = UUID.randomUUID();
        when(findUserByPersonalIdUseCase.execute("A123B"))
                .thenReturn(InternalUserIdentityResponse.builder()
                        .id(userId)
                        .personalId("A123B")
                        .build());

        mockMvc.perform(get("/internal/users/by-personal-id/{personalId}", "A123B")
                        .header("X-Service-Token", "conference-internal-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(userId.toString()))
                .andExpect(jsonPath("$.data.personalId").value("A123B"));

        verify(findUserByPersonalIdUseCase).execute("A123B");
    }

    @Test
    void should_return_401_when_service_token_header_is_missing() throws Exception {
        mockMvc.perform(get("/internal/users/by-personal-id/{personalId}", "A123B"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("auth.service_token_invalid"));

        verify(findUserByPersonalIdUseCase, never()).execute("A123B");
    }

    @Test
    void should_return_401_when_service_token_is_invalid() throws Exception {
        mockMvc.perform(get("/internal/users/by-personal-id/{personalId}", "A123B")
                        .header("X-Service-Token", "invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("auth.service_token_invalid"));

        verify(findUserByPersonalIdUseCase, never()).execute("A123B");
    }

    @Test
    void should_return_400_when_personal_id_is_invalid() throws Exception {
        when(findUserByPersonalIdUseCase.execute("A-123"))
                .thenThrow(new ApiException(HttpStatus.BAD_REQUEST, "validation.failed", "Invalid personalId format"));

        mockMvc.perform(get("/internal/users/by-personal-id/{personalId}", "A-123")
                        .header("X-Service-Token", "conference-internal-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation.failed"));
    }

    @Test
    void should_return_404_when_user_not_found() throws Exception {
        when(findUserByPersonalIdUseCase.execute("A123B"))
                .thenThrow(new ApiException(HttpStatus.NOT_FOUND, "resource.not_found", "User not found"));

        mockMvc.perform(get("/internal/users/by-personal-id/{personalId}", "A123B")
                        .header("X-Service-Token", "conference-internal-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("resource.not_found"));
    }

    @Test
    void should_not_open_unrelated_internal_routes_without_jwt() throws Exception {
        mockMvc.perform(get("/internal/ping"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("auth.token_invalid"));
    }
}
