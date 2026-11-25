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
import com.dulcepack.backend.security.ClientOriginResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SessionCookieService cookies;
    private final SessionService sessions;
    private final ClientOriginResolver origins;

    public AuthController(AuthService authService, SessionCookieService cookies, SessionService sessions,
            ClientOriginResolver origins) {
        this.authService = authService;
        this.cookies = cookies;
        this.sessions = sessions;
        this.origins = origins;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
        return new AuthResponse(UserResponse.from(authService.register(request, origins.resolve(servletRequest))));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        var result = authService.login(request, origins.resolve(servletRequest), servletRequest.getHeader("User-Agent"));
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
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request, HttpServletRequest servletRequest) {
        authService.forgotPassword(request, origins.resolve(servletRequest));
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
    }

}
