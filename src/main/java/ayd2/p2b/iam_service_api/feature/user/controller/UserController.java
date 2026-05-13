package ayd2.p2b.iam_service_api.feature.user.controller;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.common.response.ApiResponse;
import ayd2.p2b.iam_service_api.common.response.PageResponse;
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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
    @Operation(summary = "Self register participant")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(registerParticipantUseCase.execute(request)));
    }

    @GetMapping("/me")
    @Operation(summary = "Get authenticated user profile")
    public ResponseEntity<ApiResponse<UserResponse>> me(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.of(getCurrentUserUseCase.execute(getAuthenticatedUser(authentication).getUserId())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by id")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable UUID id, Authentication authentication) {
        RequesterContext requester = toRequesterContext(authentication);
        return ResponseEntity.ok(ApiResponse.of(getUserByIdUseCase.execute(requester, id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user profile")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication
    ) {
        RequesterContext requester = toRequesterContext(authentication);
        return ResponseEntity.ok(ApiResponse.of(updateUserUseCase.execute(requester, id, request)));
    }

    @GetMapping("/{id}/can-be-committee")
    @Operation(summary = "Validate if user can be committee member")
    public ResponseEntity<ApiResponse<CommitteeEligibilityResponse>> canBeCommittee(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        RequesterContext requester = toRequesterContext(authentication);
        return ResponseEntity.ok(ApiResponse.of(canBeCommitteeUseCase.execute(requester, id)));
    }

    @GetMapping
    @Operation(summary = "List users")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> list(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) UUID institutionId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Integer page,
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
    @Operation(summary = "Activate user")
    public ResponseEntity<ApiResponse<UserResponse>> activate(@PathVariable UUID id, Authentication authentication) {
        RequesterContext requester = toRequesterContext(authentication);
        return ResponseEntity.ok(ApiResponse.of(activateUserUseCase.execute(requester, id)));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate user")
    public ResponseEntity<ApiResponse<UserResponse>> deactivate(@PathVariable UUID id, Authentication authentication) {
        RequesterContext requester = toRequesterContext(authentication);
        return ResponseEntity.ok(ApiResponse.of(deactivateUserUseCase.execute(requester, id)));
    }

    @PostMapping("/system-admins")
    @Operation(summary = "Create system admin")
    public ResponseEntity<ApiResponse<UserResponse>> createSystemAdmin(
            @Valid @RequestBody CreateSystemAdminRequest request,
            Authentication authentication
    ) {
        RequesterContext requester = toRequesterContext(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(createSystemAdminUseCase.execute(requester, request)));
    }

    @PostMapping("/congress-admins")
    @Operation(summary = "Create congress admin")
    public ResponseEntity<ApiResponse<UserResponse>> createCongressAdmin(
            @Valid @RequestBody CreateCongressAdminRequest request,
            Authentication authentication
    ) {
        RequesterContext requester = toRequesterContext(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(createCongressAdminUseCase.execute(requester, request)));
    }

    @PostMapping("/guest-speakers")
    @Operation(summary = "Create guest speaker")
    public ResponseEntity<ApiResponse<UserResponse>> createGuestSpeaker(
            @Valid @RequestBody CreateGuestSpeakerRequest request,
            Authentication authentication
    ) {
        RequesterContext requester = toRequesterContext(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(createGuestSpeakerUseCase.execute(requester, request)));
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

