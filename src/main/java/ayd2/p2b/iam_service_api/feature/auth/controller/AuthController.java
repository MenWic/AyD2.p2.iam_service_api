package ayd2.p2b.iam_service_api.feature.auth.controller;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.common.response.ApiResponse;
import ayd2.p2b.iam_service_api.core.openapi.OpenApiExamples;
import ayd2.p2b.iam_service_api.feature.auth.application.login.LoginUseCase;
import ayd2.p2b.iam_service_api.feature.auth.application.logout.LogoutUseCase;
import ayd2.p2b.iam_service_api.feature.auth.application.refresh.RefreshTokenUseCase;
import ayd2.p2b.iam_service_api.feature.auth.dto.request.LoginRequest;
import ayd2.p2b.iam_service_api.feature.auth.dto.request.LogoutRequest;
import ayd2.p2b.iam_service_api.feature.auth.dto.request.RefreshRequest;
import ayd2.p2b.iam_service_api.feature.auth.dto.response.AuthResponse;
import ayd2.p2b.iam_service_api.feature.auth.dto.response.RefreshResponse;
import ayd2.p2b.iam_service_api.feature.auth.infrastructure.security.principal.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;

    @PostMapping("/login")
    @Operation(
            summary = "Login user",
            description = "Public endpoint. Authenticates user with email and password, and returns access token, refresh token, and user profile."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = OpenApiExamples.AUTH_SUCCESS))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.VALIDATION_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.INVALID_CREDENTIALS_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.INTERNAL_ERROR)))
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Login credentials",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = OpenApiExamples.LOGIN_REQUEST))
            )
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of(loginUseCase.execute(request)));
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Public endpoint at Bearer level. Authenticates using a valid refreshToken in request body and returns a new access token."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refresh successful",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = OpenApiExamples.REFRESH_SUCCESS))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.VALIDATION_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid, expired, or blacklisted refresh token",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.TOKEN_INVALID_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.INTERNAL_ERROR)))
    })
    public ResponseEntity<ApiResponse<RefreshResponse>> refresh(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Refresh token credential",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = OpenApiExamples.REFRESH_REQUEST))
            )
            @Valid @RequestBody RefreshRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of(refreshTokenUseCase.execute(request)));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout user and invalidate refresh token",
            description = "Protected endpoint. Requires Bearer JWT and a refreshToken in the request body. Invalidates the provided refresh token."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Logout successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.VALIDATION_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or missing token",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.TOKEN_INVALID_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.INTERNAL_ERROR)))
    })
    public ResponseEntity<Void> logout(
            @Parameter(
                    description = "Refresh token to invalidate",
                    required = true,
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = OpenApiExamples.REFRESH_REQUEST))
            )
            @Valid @RequestBody LogoutRequest request,
            Authentication authentication
    ) {
        if (!(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "auth.token_invalid", "Invalid authentication");
        }
        logoutUseCase.execute(authenticatedUser.getUserId(), request);
        return ResponseEntity.noContent().build();
    }
}
