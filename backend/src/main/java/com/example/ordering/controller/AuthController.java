package com.example.ordering.controller;

import com.example.ordering.domain.User;
import com.example.ordering.dto.ApiResponse;
import com.example.ordering.dto.AuthStatusResponse;
import com.example.ordering.dto.LoginRequest;
import com.example.ordering.dto.LoginResponse;
import com.example.ordering.dto.RegisterRequest;
import com.example.ordering.security.JwtTokenUtil;
import com.example.ordering.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;

    public AuthController(JwtTokenUtil jwtTokenUtil, UserService userService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.authenticate(request.getUsername(), request.getPassword());
        if (user == null) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error(401, "用户名或密码错误"));
        }
        String token = jwtTokenUtil.generateToken(user.getUsername());
        LoginResponse resp = new LoginResponse(token, user.getUsername(), user.getDisplayName());
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(
            request.getUsername(),
            request.getPassword(),
            request.getDisplayName()
        );
        String token = jwtTokenUtil.generateToken(user.getUsername());
        LoginResponse resp = new LoginResponse(token, user.getUsername(), user.getDisplayName());
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @GetMapping("/status")
    public ApiResponse<AuthStatusResponse> status() {
        return ApiResponse.success(new AuthStatusResponse(userService.hasAdmin()));
    }
}
