package com.weiji.modules.user.service; // 业务接口

import com.weiji.modules.user.dto.LoginRequest; // 本仓类 LoginRequest
import com.weiji.modules.user.dto.RefreshTokenRequest; // 本仓类 RefreshTokenRequest
import com.weiji.modules.user.dto.RegisterRequest; // 本仓类 RegisterRequest
import com.weiji.modules.user.vo.TokenVO; // 本仓类 TokenVO

public interface AuthService { // 定义接口 AuthService

    TokenVO register(RegisterRequest request); // 本行业务语句

    TokenVO login(LoginRequest request); // 本行业务语句

    TokenVO refresh(RefreshTokenRequest request); // 本行业务语句

    void logout(String accessToken); // 本行业务语句
}
