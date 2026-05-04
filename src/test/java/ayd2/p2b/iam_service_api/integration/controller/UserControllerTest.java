package ayd2.p2b.iam_service_api.integration.controller;

import ayd2.p2b.iam_service_api.application.dto.user.UserResponse;
import ayd2.p2b.iam_service_api.application.port.security.TokenParserPort;
import ayd2.p2b.iam_service_api.application.usecase.auth.RegisterParticipantUseCase;
import ayd2.p2b.iam_service_api.application.usecase.user.GetCurrentUserUseCase;
import ayd2.p2b.iam_service_api.controller.user.UserController;
import ayd2.p2b.iam_service_api.domain.model.user.Role;
import ayd2.p2b.iam_service_api.infrastructure.security.principal.AuthenticatedUser;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetCurrentUserUseCase getCurrentUserUseCase;
    @MockitoBean
    private RegisterParticipantUseCase registerParticipantUseCase;
    @MockitoBean
    private TokenParserPort tokenParserPort;

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
