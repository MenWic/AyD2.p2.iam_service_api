package ayd2.p2b.iam_service_api.feature.user.controller;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.common.response.ApiResponse;
import ayd2.p2b.iam_service_api.common.response.PageResponse;
import ayd2.p2b.iam_service_api.common.util.PrincipalResolver;
import ayd2.p2b.iam_service_api.core.openapi.OpenApiExamples;
import ayd2.p2b.iam_service_api.feature.auth.application.register.RegisterParticipantUseCase;
import ayd2.p2b.iam_service_api.feature.auth.dto.response.AuthResponse;
import ayd2.p2b.iam_service_api.feature.auth.infrastructure.security.principal.AuthenticatedUser;
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
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.dto.internal.RequesterContext;
import ayd2.p2b.iam_service_api.feature.user.dto.request.CreateCongressAdminRequest;
import ayd2.p2b.iam_service_api.feature.user.dto.request.CreateGuestSpeakerRequest;
import ayd2.p2b.iam_service_api.feature.user.dto.request.CreateSystemAdminRequest;
import ayd2.p2b.iam_service_api.feature.user.dto.internal.UserSearchCriteria;
import ayd2.p2b.iam_service_api.feature.user.dto.request.RegisterUserRequest;
import ayd2.p2b.iam_service_api.feature.user.dto.request.UpdateUserRequest;
import ayd2.p2b.iam_service_api.feature.user.dto.response.CommitteeEligibilityResponse;
import ayd2.p2b.iam_service_api.feature.user.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "Users")
public class UserController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final RegisterParticipantUseCase registerParticipantUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final ActivateUserUseCase activateUserUseCase;
    private final DeactivateUserUseCase deactivateUserUseCase;
    private final CreateSystemAdminUseCase createSystemAdminUseCase;
    private final CreateCongressAdminUseCase createCongressAdminUseCase;
    private final CreateGuestSpeakerUseCase createGuestSpeakerUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final CanBeCommitteeUseCase canBeCommitteeUseCase;

    public UserController(
            RegisterParticipantUseCase registerParticipantUseCase,
            GetCurrentUserUseCase getCurrentUserUseCase,
            GetUserByIdUseCase getUserByIdUseCase,
            ListUsersUseCase listUsersUseCase,
            ActivateUserUseCase activateUserUseCase,
            DeactivateUserUseCase deactivateUserUseCase,
            CreateSystemAdminUseCase createSystemAdminUseCase,
            CreateCongressAdminUseCase createCongressAdminUseCase,
            CreateGuestSpeakerUseCase createGuestSpeakerUseCase,
            UpdateUserUseCase updateUserUseCase,
            CanBeCommitteeUseCase canBeCommitteeUseCase
    ) {
        this.registerParticipantUseCase = registerParticipantUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.getUserByIdUseCase = getUserByIdUseCase;
        this.listUsersUseCase = listUsersUseCase;
        this.activateUserUseCase = activateUserUseCase;
        this.deactivateUserUseCase = deactivateUserUseCase;
        this.createSystemAdminUseCase = createSystemAdminUseCase;
        this.createCongressAdminUseCase = createCongressAdminUseCase;
        this.createGuestSpeakerUseCase = createGuestSpeakerUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.canBeCommitteeUseCase = canBeCommitteeUseCase;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Self register participant",
            description = "Public endpoint. Registers a PARTICIPANT. personalId is required, alphanumeric, and unique."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Participant registered",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = OpenApiExamples.AUTH_SUCCESS))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.VALIDATION_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email or personalId conflict",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.CONFLICT_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.INTERNAL_ERROR)))
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Participant registration payload",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = OpenApiExamples.REGISTER_REQUEST))
            )
            @Valid @RequestBody RegisterUserRequest request
    ) {
        AuthResponse response = registerParticipantUseCase.execute(request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/users/{id}")
                .buildAndExpand(response.getUser().getId())
                .toUri();
        return ResponseEntity.created(location).body(ApiResponse.of(response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get authenticated user profile", description = "Protected endpoint. Returns current authenticated user profile.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile fetched",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = OpenApiExamples.USER_SUCCESS))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or missing token",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.TOKEN_INVALID_ERROR)))
    })
    public ResponseEntity<ApiResponse<UserResponse>> me(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.of(getCurrentUserUseCase.execute(getAuthenticatedUser(authentication).getUserId())));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get user by id",
            description = "Protected endpoint. Allowed for self, SYSTEM_ADMIN, and institution-scoped CONGRESS_ADMIN."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = OpenApiExamples.USER_SUCCESS))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or missing token",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.TOKEN_INVALID_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.FORBIDDEN_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.NOT_FOUND_ERROR)))
    })
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable UUID id, Authentication authentication) {
        RequesterContext requester = toRequesterContext(authentication);
        return ResponseEntity.ok(ApiResponse.of(getUserByIdUseCase.execute(requester, id)));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update user profile",
            description = "Protected endpoint. Allowed for self or SYSTEM_ADMIN. Updates editable profile fields only; does not modify email, personalId, roles, active, password, or institutions."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User updated",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = OpenApiExamples.USER_SUCCESS))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.VALIDATION_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or missing token",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.TOKEN_INVALID_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.FORBIDDEN_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.NOT_FOUND_ERROR)))
    })
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication
    ) {
        RequesterContext requester = toRequesterContext(authentication);
        return ResponseEntity.ok(ApiResponse.of(updateUserUseCase.execute(requester, id, request)));
    }

    @GetMapping("/{id}/can-be-committee")
    @Operation(
            summary = "Validate if user can be committee member",
            description = "Protected endpoint. Current JWT mode allows only CONGRESS_ADMIN callers. Used by conference-service to validate committee eligibility."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Eligibility result returned",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = OpenApiExamples.COMMITTEE_ELIGIBILITY_SUCCESS))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or missing token",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.TOKEN_INVALID_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.FORBIDDEN_ERROR)))
    })
    public ResponseEntity<ApiResponse<CommitteeEligibilityResponse>> canBeCommittee(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        RequesterContext requester = toRequesterContext(authentication);
        return ResponseEntity.ok(ApiResponse.of(canBeCommitteeUseCase.execute(requester, id)));
    }

    @GetMapping
    @Operation(
            summary = "List users",
            description = "Protected endpoint. Allowed for SYSTEM_ADMIN and institution-scoped CONGRESS_ADMIN. Returns paginated users."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users listed",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = OpenApiExamples.PAGED_USERS_SUCCESS))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid query parameters",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.VALIDATION_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or missing token",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.TOKEN_INVALID_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.FORBIDDEN_ERROR)))
    })
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> list(
            @Parameter(description = "Filter by role", example = "PARTICIPANT")
            @RequestParam(required = false) Role role,
            @Parameter(description = "Filter by active status", example = "true")
            @RequestParam(required = false) Boolean active,
            @Parameter(description = "Filter by institution UUID", example = "22222222-2222-2222-2222-222222222222")
            @RequestParam(required = false) UUID institutionId,
            @Parameter(description = "Search by name, email, or personalId", example = "jane")
            @RequestParam(required = false) String search,
            @Parameter(description = "Page number (default: 0)", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size (default: 20, max: 100)", example = "20")
            @RequestParam(defaultValue = "20") Integer size,
            Authentication authentication
    ) {
        RequesterContext requester = toRequesterContext(authentication);
        Pageable pageable = PageRequest.of(
                Math.max(page == null ? DEFAULT_PAGE : page, DEFAULT_PAGE),
                normalizeSize(size)
        );
        UserSearchCriteria criteria = UserSearchCriteria.builder()
                .role(role)
                .active(active)
                .institutionId(institutionId)
                .search(search)
                .build();

        return ResponseEntity.ok(ApiResponse.of(listUsersUseCase.execute(requester, criteria, pageable)));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate user", description = "Protected endpoint. Allowed only for SYSTEM_ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User activated",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = OpenApiExamples.USER_SUCCESS))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or missing token",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.TOKEN_INVALID_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.FORBIDDEN_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.NOT_FOUND_ERROR)))
    })
    public ResponseEntity<ApiResponse<UserResponse>> activate(@PathVariable UUID id, Authentication authentication) {
        RequesterContext requester = toRequesterContext(authentication);
        return ResponseEntity.ok(ApiResponse.of(activateUserUseCase.execute(requester, id)));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(
            summary = "Deactivate user",
            description = "Protected endpoint. Allowed only for SYSTEM_ADMIN. Rejects request if it would leave zero active SYSTEM_ADMIN users."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User deactivated",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = OpenApiExamples.USER_SUCCESS))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or missing token",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.TOKEN_INVALID_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.FORBIDDEN_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.NOT_FOUND_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Invariant violation (last active SYSTEM_ADMIN)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.CONFLICT_ERROR)))
    })
    public ResponseEntity<ApiResponse<UserResponse>> deactivate(@PathVariable UUID id, Authentication authentication) {
        RequesterContext requester = toRequesterContext(authentication);
        return ResponseEntity.ok(ApiResponse.of(deactivateUserUseCase.execute(requester, id)));
    }

    @PostMapping("/system-admins")
    @Operation(summary = "Create system admin", description = "Protected endpoint. Allowed only for SYSTEM_ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "System admin created",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = OpenApiExamples.USER_SUCCESS))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.VALIDATION_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or missing token",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.TOKEN_INVALID_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.FORBIDDEN_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email or personalId conflict",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.CONFLICT_ERROR)))
    })
    public ResponseEntity<ApiResponse<UserResponse>> createSystemAdmin(
            @Valid @RequestBody CreateSystemAdminRequest request,
            Authentication authentication
    ) {
        RequesterContext requester = toRequesterContext(authentication);
        UserResponse response = createSystemAdminUseCase.execute(requester, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/users/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(ApiResponse.of(response));
    }

    @PostMapping("/congress-admins")
    @Operation(
            summary = "Create congress admin",
            description = "Protected endpoint. Allowed only for SYSTEM_ADMIN. Creates CONGRESS_ADMIN and links institutions. institutionIds is required and non-empty."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Congress admin created",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = OpenApiExamples.USER_SUCCESS))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.VALIDATION_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or missing token",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.TOKEN_INVALID_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.FORBIDDEN_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email or personalId conflict",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.CONFLICT_ERROR)))
    })
    public ResponseEntity<ApiResponse<UserResponse>> createCongressAdmin(
            @Valid @RequestBody CreateCongressAdminRequest request,
            Authentication authentication
    ) {
        RequesterContext requester = toRequesterContext(authentication);
        UserResponse response = createCongressAdminUseCase.execute(requester, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/users/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(ApiResponse.of(response));
    }

    @PostMapping("/guest-speakers")
    @Operation(
            summary = "Create guest speaker",
            description = "Protected endpoint. Allowed only for CONGRESS_ADMIN. Creates GUEST_SPEAKER. Password is optional and may be null/blank."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Guest speaker created",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = OpenApiExamples.USER_SUCCESS))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.VALIDATION_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or missing token",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.TOKEN_INVALID_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.FORBIDDEN_ERROR))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email or personalId conflict",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = OpenApiExamples.CONFLICT_ERROR)))
    })
    public ResponseEntity<ApiResponse<UserResponse>> createGuestSpeaker(
            @Valid @RequestBody CreateGuestSpeakerRequest request,
            Authentication authentication
    ) {
        RequesterContext requester = toRequesterContext(authentication);
        UserResponse response = createGuestSpeakerUseCase.execute(requester, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/users/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(ApiResponse.of(response));
    }

    private int normalizeSize(Integer size) {
        if (size == null || size <= 0) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    private RequesterContext toRequesterContext(Authentication authentication) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(authentication);
        return new RequesterContext(authenticatedUser.getUserId(), authenticatedUser.getRoles());
    }

    private AuthenticatedUser getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "auth.token_invalid", "Invalid authentication");
        }
        return authenticatedUser;
    }
}

