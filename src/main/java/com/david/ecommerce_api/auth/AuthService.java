package com.david.ecommerce_api.auth;

import com.david.ecommerce_api.auth.AuthDtos.AuthResponse;
import com.david.ecommerce_api.auth.AuthDtos.LoginRequest;
import com.david.ecommerce_api.auth.AuthDtos.PasswordRequest;
import com.david.ecommerce_api.auth.AuthDtos.ProfileRequest;
import com.david.ecommerce_api.auth.AuthDtos.RegisterRequest;
import com.david.ecommerce_api.auth.AuthDtos.UserResponse;
import com.david.ecommerce_api.common.error.DomainException;
import com.david.ecommerce_api.common.error.ErrorCode;
import com.david.ecommerce_api.common.validation.Guard;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = User.normalizeEmail(request.email());

        Guard.isFalse(userRepository.existsByEmail(email), ErrorCode.AUTH_EMAIL_TAKEN);

        User user = userRepository.save(
                User.create(request.name(), email, passwordEncoder.encode(request.password()), Role.CUSTOMER)
        );

        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(User.normalizeEmail(request.email()))
                .filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
                .orElseThrow(() -> new DomainException(ErrorCode.AUTH_INVALID_CREDENTIALS));

        Guard.isFalse(user.isBlocked(), ErrorCode.AUTH_ACCOUNT_BLOCKED);

        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse me(Long userId) {
        return UserResponse.from(findUser(userId));
    }

    @Transactional
    public UserResponse updateProfile(Long userId, ProfileRequest request) {
        User user = findUser(userId);
        String email = User.normalizeEmail(request.email());

        Guard.isFalse(userRepository.existsByEmailAndIdNot(email, userId), ErrorCode.AUTH_EMAIL_TAKEN);

        user.updateProfile(request.name(), email);

        return UserResponse.from(user);
    }

    @Transactional
    public void changePassword(Long userId, PasswordRequest request) {
        User user = findUser(userId);

        Guard.isTrue(
                passwordEncoder.matches(request.current(), user.getPasswordHash()),
                ErrorCode.AUTH_WRONG_PASSWORD
        );

        user.changePasswordHash(passwordEncoder.encode(request.next()));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new DomainException(ErrorCode.USER_NOT_FOUND));
    }

    private AuthResponse toResponse(User user) {
        return new AuthResponse(jwtService.issue(user), UserResponse.from(user));
    }
}
