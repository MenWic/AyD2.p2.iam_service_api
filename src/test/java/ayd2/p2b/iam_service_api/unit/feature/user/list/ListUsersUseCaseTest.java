package ayd2.p2b.iam_service_api.unit.feature.user.list;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.common.response.PageResponse;
import ayd2.p2b.iam_service_api.feature.user.application.list.ListUsersUseCase;
import ayd2.p2b.iam_service_api.feature.user.application.port.UserRepositoryPort;
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import ayd2.p2b.iam_service_api.feature.user.domain.model.UserAccount;
import ayd2.p2b.iam_service_api.feature.user.dto.internal.RequesterContext;
import ayd2.p2b.iam_service_api.feature.user.dto.internal.UserSearchCriteria;
import ayd2.p2b.iam_service_api.feature.user.dto.response.UserResponse;
import ayd2.p2b.iam_service_api.feature.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListUsersUseCaseTest {

    @Mock private UserRepositoryPort userRepository;
    @Mock private UserMapper userMapper;

    private ListUsersUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListUsersUseCase(userRepository, userMapper);
    }

    @Test
    void should_return_page_when_requester_is_system_admin() {
        RequesterContext requester = new RequesterContext(UUID.randomUUID(), Set.of(Role.SYSTEM_ADMIN));
        UserAccount user = user(UUID.randomUUID(), Set.of(Role.PARTICIPANT), Set.of());
        UserResponse userResponse = response(user.getId());
        Pageable pageable = PageRequest.of(0, 20);

        when(userRepository.findAll(any(UserSearchCriteria.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(user), pageable, 1));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        PageResponse<UserResponse> result = useCase.execute(requester, UserSearchCriteria.builder().build(), pageable);

        assertEquals(1, result.getItems().size());
        assertEquals(1L, result.getTotalItems());
        assertEquals(user.getId(), result.getItems().getFirst().getId());
    }

    @Test
    void should_apply_role_filter() {
        RequesterContext requester = new RequesterContext(UUID.randomUUID(), Set.of(Role.SYSTEM_ADMIN));
        Pageable pageable = PageRequest.of(0, 20);
        UserSearchCriteria criteria = UserSearchCriteria.builder().role(Role.PARTICIPANT).build();

        when(userRepository.findAll(any(UserSearchCriteria.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        useCase.execute(requester, criteria, pageable);

        ArgumentCaptor<UserSearchCriteria> captor = ArgumentCaptor.forClass(UserSearchCriteria.class);
        verify(userRepository).findAll(captor.capture(), eq(pageable));
        assertEquals(Role.PARTICIPANT, captor.getValue().getRole());
    }

    @Test
    void should_apply_active_filter() {
        RequesterContext requester = new RequesterContext(UUID.randomUUID(), Set.of(Role.SYSTEM_ADMIN));
        Pageable pageable = PageRequest.of(0, 20);
        UserSearchCriteria criteria = UserSearchCriteria.builder().active(Boolean.TRUE).build();

        when(userRepository.findAll(any(UserSearchCriteria.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        useCase.execute(requester, criteria, pageable);

        ArgumentCaptor<UserSearchCriteria> captor = ArgumentCaptor.forClass(UserSearchCriteria.class);
        verify(userRepository).findAll(captor.capture(), eq(pageable));
        assertEquals(Boolean.TRUE, captor.getValue().getActive());
    }

    @Test
    void should_apply_institution_filter() {
        RequesterContext requester = new RequesterContext(UUID.randomUUID(), Set.of(Role.SYSTEM_ADMIN));
        Pageable pageable = PageRequest.of(0, 20);
        UUID institutionId = UUID.randomUUID();
        UserSearchCriteria criteria = UserSearchCriteria.builder().institutionId(institutionId).build();

        when(userRepository.findAll(any(UserSearchCriteria.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        useCase.execute(requester, criteria, pageable);

        ArgumentCaptor<UserSearchCriteria> captor = ArgumentCaptor.forClass(UserSearchCriteria.class);
        verify(userRepository).findAll(captor.capture(), eq(pageable));
        assertEquals(institutionId, captor.getValue().getInstitutionId());
    }

    @Test
    void should_apply_search_filter() {
        RequesterContext requester = new RequesterContext(UUID.randomUUID(), Set.of(Role.SYSTEM_ADMIN));
        Pageable pageable = PageRequest.of(0, 20);
        UserSearchCriteria criteria = UserSearchCriteria.builder().search("john").build();

        when(userRepository.findAll(any(UserSearchCriteria.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        useCase.execute(requester, criteria, pageable);

        ArgumentCaptor<UserSearchCriteria> captor = ArgumentCaptor.forClass(UserSearchCriteria.class);
        verify(userRepository).findAll(captor.capture(), eq(pageable));
        assertEquals("john", captor.getValue().getSearch());
    }

    @Test
    void should_scope_results_when_requester_is_congress_admin() {
        UUID requesterId = UUID.randomUUID();
        UUID institutionA = UUID.randomUUID();
        UUID institutionB = UUID.randomUUID();
        RequesterContext requester = new RequesterContext(requesterId, Set.of(Role.CONGRESS_ADMIN));
        UserAccount requesterAccount = user(requesterId, Set.of(Role.CONGRESS_ADMIN), Set.of(institutionA, institutionB));
        Pageable pageable = PageRequest.of(0, 20);

        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requesterAccount));
        when(userRepository.findAll(any(UserSearchCriteria.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        useCase.execute(requester, UserSearchCriteria.builder().build(), pageable);

        ArgumentCaptor<UserSearchCriteria> captor = ArgumentCaptor.forClass(UserSearchCriteria.class);
        verify(userRepository).findAll(captor.capture(), eq(pageable));
        assertEquals(Set.of(institutionA, institutionB), captor.getValue().getScopedInstitutionIds());
    }

    @Test
    void should_return_empty_page_when_congress_admin_has_no_linked_institutions() {
        UUID requesterId = UUID.randomUUID();
        RequesterContext requester = new RequesterContext(requesterId, Set.of(Role.CONGRESS_ADMIN));
        UserAccount requesterAccount = user(requesterId, Set.of(Role.CONGRESS_ADMIN), Set.of());

        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requesterAccount));

        PageResponse<UserResponse> result = useCase.execute(
                requester,
                UserSearchCriteria.builder().build(),
                PageRequest.of(0, 20)
        );

        assertTrue(result.getItems().isEmpty());
        assertEquals(0L, result.getTotalItems());
        verify(userRepository, never()).findAll(any(UserSearchCriteria.class), any(Pageable.class));
    }

    @Test
    void should_throw_forbidden_when_congress_admin_filters_outside_scope() {
        UUID requesterId = UUID.randomUUID();
        UUID allowedInstitution = UUID.randomUUID();
        UUID blockedInstitution = UUID.randomUUID();
        RequesterContext requester = new RequesterContext(requesterId, Set.of(Role.CONGRESS_ADMIN));
        UserAccount requesterAccount = user(requesterId, Set.of(Role.CONGRESS_ADMIN), Set.of(allowedInstitution));
        UserSearchCriteria criteria = UserSearchCriteria.builder().institutionId(blockedInstitution).build();

        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requesterAccount));

        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(requester, criteria, PageRequest.of(0, 20)));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("auth.forbidden", ex.getCode());
        verify(userRepository, never()).findAll(any(UserSearchCriteria.class), any(Pageable.class));
    }

    @Test
    void should_throw_forbidden_when_role_has_no_listing_permission() {
        RequesterContext requester = new RequesterContext(UUID.randomUUID(), Set.of(Role.PARTICIPANT));

        ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(
                requester,
                UserSearchCriteria.builder().build(),
                PageRequest.of(0, 20)
        ));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("auth.forbidden", ex.getCode());
    }

    @Test
    void should_throw_forbidden_when_requester_is_null() {
        ApiException ex = assertThrows(
                ApiException.class,
                () -> useCase.execute(null, UserSearchCriteria.builder().build(), PageRequest.of(0, 20))
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("auth.forbidden", ex.getCode());
    }

    @Test
    void should_throw_validation_failed_when_pageable_is_null() {
        RequesterContext requester = new RequesterContext(UUID.randomUUID(), Set.of(Role.SYSTEM_ADMIN));

        ApiException ex = assertThrows(
                ApiException.class,
                () -> useCase.execute(requester, UserSearchCriteria.builder().build(), null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("validation.failed", ex.getCode());
    }

    @Test
    void should_throw_forbidden_when_congress_admin_has_null_user_id() {
        RequesterContext requester = new RequesterContext(null, Set.of(Role.CONGRESS_ADMIN));

        ApiException ex = assertThrows(
                ApiException.class,
                () -> useCase.execute(requester, UserSearchCriteria.builder().build(), PageRequest.of(0, 20))
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("auth.forbidden", ex.getCode());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void should_use_safe_default_when_criteria_is_null() {
        RequesterContext requester = new RequesterContext(UUID.randomUUID(), Set.of(Role.SYSTEM_ADMIN));
        Pageable pageable = PageRequest.of(0, 20);

        when(userRepository.findAll(any(UserSearchCriteria.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        useCase.execute(requester, null, pageable);

        ArgumentCaptor<UserSearchCriteria> captor = ArgumentCaptor.forClass(UserSearchCriteria.class);
        verify(userRepository).findAll(captor.capture(), eq(pageable));
        assertNull(captor.getValue().getRole());
        assertNull(captor.getValue().getActive());
        assertNull(captor.getValue().getInstitutionId());
        assertNull(captor.getValue().getSearch());
    }

    private UserAccount user(UUID id, Set<Role> roles, Set<UUID> institutions) {
        return UserAccount.builder()
                .id(id)
                .email(id + "@domain.com")
                .active(true)
                .roles(roles)
                .linkedInstitutions(institutions)
                .build();
    }

    private UserResponse response(UUID id) {
        return UserResponse.builder()
                .id(id)
                .email(id + "@domain.com")
                .roles(Set.of("PARTICIPANT"))
                .build();
    }
}
