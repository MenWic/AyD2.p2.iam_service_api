package ayd2.p2b.iam_service_api.feature.user.controller;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.feature.auth.application.register.RegisterParticipantUseCase;
import ayd2.p2b.iam_service_api.feature.auth.dto.response.AuthResponse;
import ayd2.p2b.iam_service_api.feature.auth.infrastructure.security.principal.AuthenticatedUser;
import ayd2.p2b.iam_service_api.feature.user.application.current.GetCurrentUserUseCase;
import ayd2.p2b.iam_service_api.feature.user.dto.request.RegisterUserRequest;
import ayd2.p2b.iam_service_api.feature.user.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Tag(name = "Users")
public class UserController {

    private final RegisterParticipantUseCase registerParticipantUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;

    public UserController(RegisterParticipantUseCase registerParticipantUseCase, GetCurrentUserUseCase getCurrentUserUseCase) {
        this.registerParticipantUseCase = registerParticipantUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
    }

    @PostMapping("/register")
    @Operation(summary = "Self register participant")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registerParticipantUseCase.execute(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Get authenticated user profile")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        if (authentication == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "auth.token_invalid", "Invalid authentication");
        }
        if (!(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "auth.token_invalid", "Invalid authentication");
        }
        return ResponseEntity.ok(getCurrentUserUseCase.execute(authenticatedUser.userId()));
    }
}

