package com.david.ecommerce_api.auth;

import com.david.ecommerce_api.auth.AuthDtos.AuthResponse;
import com.david.ecommerce_api.auth.AuthDtos.LoginRequest;
import com.david.ecommerce_api.auth.AuthDtos.PasswordRequest;
import com.david.ecommerce_api.auth.AuthDtos.ProfileRequest;
import com.david.ecommerce_api.auth.AuthDtos.RegisterRequest;
import com.david.ecommerce_api.auth.AuthDtos.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthenticatedUser principal) {
        return authService.me(principal.id());
    }

    @PutMapping("/profile")
    public UserResponse updateProfile(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody ProfileRequest request
    ) {
        return authService.updateProfile(principal.id(), request);
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody PasswordRequest request
    ) {
        authService.changePassword(principal.id(), request);
    }
}
