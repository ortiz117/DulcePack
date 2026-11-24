package com.dulcepack.backend.identity.api;

import java.time.Instant;
import java.util.UUID;

import com.dulcepack.backend.identity.domain.UserEntity;
import com.dulcepack.backend.identity.domain.UserRole;
import com.dulcepack.backend.identity.domain.UserStatus;

public record UserResponse(
        UUID id,
        String email,
        String displayName,
        UserRole role,
        UserStatus status,
        Instant createdAt) {

    public static UserResponse from(UserEntity user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getRole(),
                user.getStatus(), user.getCreatedAt());
    }
}
