package com.weiji.modules.user.service;

import com.weiji.modules.user.dto.LoginRequest;
import com.weiji.modules.user.dto.RefreshTokenRequest;
import com.weiji.modules.user.dto.RegisterRequest;
import com.weiji.modules.user.vo.TokenVO;

public interface AuthService {

    TokenVO register(RegisterRequest request);

    TokenVO login(LoginRequest request);

    TokenVO refresh(RefreshTokenRequest request);

    void logout(String accessToken);
}
