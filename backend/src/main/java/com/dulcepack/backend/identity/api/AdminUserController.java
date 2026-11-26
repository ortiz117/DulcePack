package com.dulcepack.backend.identity.api;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dulcepack.backend.identity.service.AdminUserService;
import com.dulcepack.backend.security.CurrentUser;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final AdminUserService adminUsers;

    public AdminUserController(AdminUserService adminUsers) {
        this.adminUsers = adminUsers;
    }

    @GetMapping
    public PagedResponse<UserResponse> list(@PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return PagedResponse.from(adminUsers.list(pageable));
    }

    @PatchMapping("/{id}/status")
    public UserResponse changeStatus(@AuthenticationPrincipal CurrentUser administrator,
            @PathVariable UUID id, @Valid @RequestBody UpdateUserStatusRequest request) {
        return adminUsers.changeStatus(administrator.id(), id, request.status());
    }
}
