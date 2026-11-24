package com.dulcepack.backend.security;

import java.util.UUID;

import com.dulcepack.backend.identity.domain.UserRole;

public record CurrentUser(UUID id, String email, UserRole role) {
}
