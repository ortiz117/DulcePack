package com.dulcepack.backend.identity.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dulcepack.backend.identity.repository.UserRepository;
import com.dulcepack.backend.security.CurrentUser;
import com.dulcepack.backend.shared.error.NotFoundException;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository users;

    public UserController(UserRepository users) {
        this.users = users;
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal CurrentUser currentUser) {
        return users.findById(currentUser.id())
                .map(UserResponse::from)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User was not found"));
    }
}
