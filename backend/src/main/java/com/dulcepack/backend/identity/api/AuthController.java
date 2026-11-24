package com.dulcepack.backend.identity.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dulcepack.backend.identity.service.AuthService;
import com.dulcepack.backend.security.SessionCookieService;
import com.dulcepack.backend.security.SessionAuthenticationFilter;
import com.dulcepack.backend.security.SessionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SessionCookieService cookies;
    private final SessionService sessions;

    public AuthController(AuthService authService, SessionCookieService cookies, SessionService sessions) {
        this.authService = authService;
        this.cookies = cookies;
        this.sessions = sessions;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return new AuthResponse(UserResponse.from(authService.register(request)));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        var result = authService.login(request, clientIp(servletRequest), servletRequest.getHeader("User-Agent"));
        cookies.write(servletResponse, result.tokens());
        return new AuthResponse(UserResponse.from(result.user()));
    }

    @PostMapping("/refresh")
    public void refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = SessionAuthenticationFilter.cookie(request, SessionCookieService.REFRESH_COOKIE);
        cookies.write(response, sessions.rotate(refreshToken));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        sessions.revoke(
                SessionAuthenticationFilter.cookie(request, SessionCookieService.ACCESS_COOKIE),
                SessionAuthenticationFilter.cookie(request, SessionCookieService.REFRESH_COOKIE));
        cookies.clear(response);
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded == null || forwarded.isBlank()
                ? request.getRemoteAddr()
                : forwarded.split(",", 2)[0].strip();
    }
}
