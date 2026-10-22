package com.david.ecommerce_api.auth;

import com.david.ecommerce_api.auth.AuthDtos.BlockRequest;
import com.david.ecommerce_api.auth.AuthDtos.UserResponse;
import com.david.ecommerce_api.common.error.DomainException;
import com.david.ecommerce_api.common.error.ErrorCode;
import com.david.ecommerce_api.common.validation.Guard;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    @PatchMapping("/{id}/blocked")
    @Transactional
    public UserResponse setBlocked(@PathVariable Long id, @RequestBody BlockRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new DomainException(ErrorCode.USER_NOT_FOUND));

        Guard.isFalse(user.isAdmin() && request.blocked(), ErrorCode.USER_ADMIN_CANNOT_BE_BLOCKED);

        user.setBlocked(request.blocked());

        return UserResponse.from(user);
    }
}
