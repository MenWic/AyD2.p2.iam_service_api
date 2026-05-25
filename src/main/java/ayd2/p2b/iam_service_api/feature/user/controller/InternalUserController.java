package ayd2.p2b.iam_service_api.feature.user.controller;

import ayd2.p2b.iam_service_api.common.response.ApiResponse;
import ayd2.p2b.iam_service_api.core.openapi.OpenApiExamples;
import ayd2.p2b.iam_service_api.core.security.InternalServiceTokenValidator;
import ayd2.p2b.iam_service_api.feature.user.application.find_by_personal_id.FindUserByPersonalIdUseCase;
import ayd2.p2b.iam_service_api.feature.user.dto.response.InternalUserIdentityResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
@Tag(name = "Internal Users")
@RequiredArgsConstructor
public class InternalUserController {

    private final InternalServiceTokenValidator internalServiceTokenValidator;
    private final FindUserByPersonalIdUseCase findUserByPersonalIdUseCase;

    @GetMapping("/by-personal-id/{personalId}")
    @Operation(
            summary = "Resolve active user by personalId for internal service integration",
            description = "Internal service-to-service endpoint. Requires X-Service-Token and returns only minimal identity data."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User identity resolved", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = OpenApiExamples.INTERNAL_USER_IDENTITY_SUCCESS))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.VALIDATION_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or invalid service token", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.SERVICE_TOKEN_INVALID_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Active user not found", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.NOT_FOUND_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Multiple active users found for personalId", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = OpenApiExamples.CONFLICT_ERROR)))
    })
    public ResponseEntity<ApiResponse<InternalUserIdentityResponse>> getByPersonalId(
            @PathVariable String personalId,
            @Parameter(
                    description = "Internal service token shared between trusted services",
                    required = true,
                    example = "conference-internal-token"
            )
            @RequestHeader(name = "X-Service-Token", required = false) String serviceToken) {
        internalServiceTokenValidator.validate(serviceToken);
        InternalUserIdentityResponse response = findUserByPersonalIdUseCase.execute(personalId);
        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
