package com.dulcepack.backend.identity.api;

import com.dulcepack.backend.identity.domain.UserStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(@NotNull UserStatus status) {
}
