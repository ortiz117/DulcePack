package com.dulcepack.backend.identity.service;

import java.time.Clock;
import java.time.Duration;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dulcepack.backend.identity.api.RegisterRequest;
import com.dulcepack.backend.identity.api.LoginRequest;
import com.dulcepack.backend.identity.api.ForgotPasswordRequest;
import com.dulcepack.backend.identity.api.ResetPasswordRequest;
import com.dulcepack.backend.identity.domain.PasswordResetTokenEntity;
import com.dulcepack.backend.identity.domain.UserEntity;
import com.dulcepack.backend.identity.domain.UserStatus;
import com.dulcepack.backend.identity.repository.UserRepository;
import com.dulcepack.backend.identity.repository.PasswordResetTokenRepository;
import com.dulcepack.backend.security.SecurityProperties;
import com.dulcepack.backend.security.TokenService;
import com.dulcepack.backend.security.SessionService;
import com.dulcepack.backend.security.SessionTokens;
import com.dulcepack.backend.shared.error.ConflictException;
import com.dulcepack.backend.shared.error.UnauthorizedException;
import com.dulcepack.backend.shared.error.BadRequestException;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final SessionService sessions;
    private final PasswordResetTokenRepository passwordResetTokens;
    private final TokenService tokens;
    private final SecurityProperties securityProperties;
    private final PasswordResetNotifier passwordResetNotifier;
    private final LoginAttemptService loginAttempts;

    private static final String DUMMY_PASSWORD_HASH = "$2a$12$dxB6zM0F2XQ9i4A1jzR0Je6AfE4fD7bp7I4LxM0q1o3TYs19T1Jcu";

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, Clock clock, SessionService sessions,
            PasswordResetTokenRepository passwordResetTokens, TokenService tokens,
            SecurityProperties securityProperties, PasswordResetNotifier passwordResetNotifier,
            LoginAttemptService loginAttempts) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
        this.sessions = sessions;
        this.passwordResetTokens = passwordResetTokens;
        this.tokens = tokens;
        this.securityProperties = securityProperties;
        this.passwordResetNotifier = passwordResetNotifier;
        this.loginAttempts = loginAttempts;
    }

    @Transactional
    public UserEntity register(RegisterRequest request) {
        String email = EmailNormalizer.normalize(request.email());
        if (users.existsByEmail(email)) {
            throw new ConflictException("EMAIL_ALREADY_REGISTERED", "Email is already registered");
        }
        UserEntity user = UserEntity.client(email, request.displayName().strip(),
                passwordEncoder.encode(request.password()), clock.instant());
        return users.save(user);
    }

    @Transactional
    public LoginResult login(LoginRequest request, String ipAddress, String userAgent) {
        String email = EmailNormalizer.normalize(request.email());
        String attemptIdentifier = loginAttempts.identifier(email, ipAddress);
        loginAttempts.assertAllowed(attemptIdentifier);
        UserEntity user = users.findByEmail(email).orElse(null);
        String hash = user == null ? DUMMY_PASSWORD_HASH : user.getPasswordHash();
        if (!passwordEncoder.matches(request.password(), hash) || user == null) {
            loginAttempts.recordFailure(attemptIdentifier);
            throw new UnauthorizedException("INVALID_CREDENTIALS", "Email or password is incorrect");
        }
        loginAttempts.recordSuccess(attemptIdentifier);
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("ACCOUNT_SUSPENDED", "Account is suspended");
        }
        return new LoginResult(user, sessions.create(user, ipAddress, userAgent));
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = EmailNormalizer.normalize(request.email());
        users.findByEmail(email).ifPresent(user -> {
            var now = clock.instant();
            passwordResetTokens.invalidateAllByUserId(user.getId(), now);
            String rawToken = tokens.generate();
            passwordResetTokens.save(PasswordResetTokenEntity.create(user, tokens.hash(rawToken),
                    now.plus(Duration.ofMinutes(securityProperties.passwordResetMinutes())), now));
            passwordResetNotifier.send(user.getEmail(), rawToken);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        var now = clock.instant();
        PasswordResetTokenEntity resetToken = passwordResetTokens
                .findByTokenHashAndUsedAtIsNull(tokens.hash(request.token()))
                .filter(value -> value.getExpiresAt().isAfter(now))
                .orElseThrow(() -> new BadRequestException("INVALID_RESET_TOKEN", "Reset token is invalid or expired"));
        UserEntity user = resetToken.getUser();
        user.changePassword(passwordEncoder.encode(request.newPassword()), now);
        resetToken.use(now);
        passwordResetTokens.invalidateAllByUserId(user.getId(), now);
        sessions.revokeAll(user);
    }

    public record LoginResult(UserEntity user, SessionTokens tokens) {
    }
}
