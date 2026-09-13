package com.weiji.modules.user.controller; // HTTP 接口层

import com.weiji.common.result.Result; // 本仓类 Result
import com.weiji.modules.user.dto.LoginRequest; // 本仓类 LoginRequest
import com.weiji.modules.user.dto.RefreshTokenRequest; // 本仓类 RefreshTokenRequest
import com.weiji.modules.user.dto.RegisterRequest; // 本仓类 RegisterRequest
import com.weiji.modules.user.service.AuthService; // 本仓类 AuthService
import com.weiji.modules.user.vo.TokenVO; // 本仓类 TokenVO
import jakarta.servlet.http.HttpServletRequest; // Servlet API
import jakarta.validation.Valid; // Bean Validation
import lombok.RequiredArgsConstructor; // Lombok 样板代码生成
import org.apache.commons.lang3.StringUtils; // 字符串工具
import org.springframework.http.HttpHeaders; // HTTP 类型
import org.springframework.web.bind.annotation.PostMapping; // Web 映射注解
import org.springframework.web.bind.annotation.RequestBody; // Web 映射注解
import org.springframework.web.bind.annotation.RequestMapping; // Web 映射注解
import org.springframework.web.bind.annotation.RestController; // Web 映射注解

@RestController // REST 控制器，返回值写入 HTTP 体
@RequestMapping("/api/auth") // URL 前缀
@RequiredArgsConstructor // Lombok：为 final 字段生成构造器注入
public class AuthController { // 定义类 AuthController

    private final AuthService authService; // 构造注入 authService

    @PostMapping("/register") // 处理 POST
    public Result<TokenVO> register(@Valid @RequestBody RegisterRequest request) { // 方法 register
        return Result.ok(authService.register(request)); // 成功响应 code=0
    }

    @PostMapping("/login") // 处理 POST
    public Result<TokenVO> login(@Valid @RequestBody LoginRequest request) { // 方法 login
        return Result.ok(authService.login(request)); // 成功响应 code=0
    }

    @PostMapping("/refresh") // 处理 POST
    public Result<TokenVO> refresh(@Valid @RequestBody RefreshTokenRequest request) { // 方法 refresh
        return Result.ok(authService.refresh(request)); // 成功响应 code=0
    }

    @PostMapping("/logout") // 处理 POST
    public Result<Void> logout(HttpServletRequest request) { // 方法 logout
        String header = request.getHeader(HttpHeaders.AUTHORIZATION); // 读 Authorization 头
        String token = StringUtils.startsWithIgnoreCase(header, "Bearer ") ? header.substring(7).trim() : null; // 去掉 Bearer 前缀得到 token
        authService.logout(token); // 本行业务语句
        return Result.ok(); // 成功响应 code=0
    }
}
