package com.dulcepack.backend.identity.service;

import java.time.Clock;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dulcepack.backend.identity.api.UserResponse;
import com.dulcepack.backend.identity.domain.UserEntity;
import com.dulcepack.backend.identity.domain.UserStatus;
import com.dulcepack.backend.identity.repository.UserRepository;
import com.dulcepack.backend.security.SessionService;
import com.dulcepack.backend.shared.error.BadRequestException;
import com.dulcepack.backend.shared.error.NotFoundException;

@Service
public class AdminUserService {
    private final UserRepository users;
    private final SessionService sessions;
    private final Clock clock;

    public AdminUserService(UserRepository users, SessionService sessions, Clock clock) {
        this.users = users;
        this.sessions = sessions;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> list(Pageable pageable) {
        return users.findAll(pageable).map(UserResponse::from);
    }

    @Transactional
    public UserResponse changeStatus(UUID administratorId, UUID userId, UserStatus status) {
        if (administratorId.equals(userId) && status == UserStatus.SUSPENDED) {
            throw new BadRequestException("ADMIN_SELF_SUSPENSION", "Administrators cannot suspend themselves");
        }
        UserEntity user = users.findById(userId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User was not found"));
        user.changeStatus(status, clock.instant());
        if (status == UserStatus.SUSPENDED) {
            sessions.revokeAll(user);
        }
        return UserResponse.from(user);
    }
}
