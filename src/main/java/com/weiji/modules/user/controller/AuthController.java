package com.weiji.modules.user.controller;

import com.weiji.common.result.Result;
import com.weiji.modules.user.dto.LoginRequest;
import com.weiji.modules.user.dto.RefreshTokenRequest;
import com.weiji.modules.user.dto.RegisterRequest;
import com.weiji.modules.user.service.AuthService;
import com.weiji.modules.user.vo.TokenVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<TokenVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok(authService.register(request));
    }

    @PostMapping("/login")
    public Result<TokenVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public Result<TokenVO> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return Result.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = StringUtils.startsWithIgnoreCase(header, "Bearer ") ? header.substring(7).trim() : null;
        authService.logout(token);
        return Result.ok();
    }
}
