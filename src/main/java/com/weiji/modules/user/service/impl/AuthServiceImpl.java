package com.weiji.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.weiji.common.constant.RedisKey;
import com.weiji.common.enums.ErrorCode;
import com.weiji.common.enums.UserStatus;
import com.weiji.common.exception.BizException;
import com.weiji.common.utils.IdUtils;
import com.weiji.common.utils.PhoneUtils;
import com.weiji.framework.redis.RedisUtils;
import com.weiji.framework.security.JwtUtils;
import com.weiji.modules.point.entity.PointAccount;
import com.weiji.modules.point.mapper.PointAccountMapper;
import com.weiji.modules.user.dto.LoginRequest;
import com.weiji.modules.user.dto.RefreshTokenRequest;
import com.weiji.modules.user.dto.RegisterRequest;
import com.weiji.modules.user.entity.User;
import com.weiji.modules.user.entity.UserPreference;
import com.weiji.modules.user.mapper.UserMapper;
import com.weiji.modules.user.mapper.UserPreferenceMapper;
import com.weiji.modules.user.service.AuthService;
import com.weiji.modules.user.vo.TokenVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final UserPreferenceMapper userPreferenceMapper;
    private final PointAccountMapper pointAccountMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RedisUtils redisUtils;

    @Override
    @Transactional
    public TokenVO register(RegisterRequest request) {
        if (!PhoneUtils.isMobile(request.getPhone())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "手机号格式不正确");
        }
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone()));
        if (exists != null && exists > 0) {
            throw new BizException(ErrorCode.USER_PHONE_EXISTS);
        }
        User user = new User();
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(StringUtils.defaultIfBlank(request.getNickname(), "微积用户" + request.getPhone().substring(7)));
        user.setStatus(UserStatus.ENABLED.getCode());
        user.setDeleted(0);
        userMapper.insert(user);

        UserPreference preference = new UserPreference();
        preference.setUserId(user.getId());
        preference.setEnergyStatus("ENERGETIC");
        userPreferenceMapper.insert(preference);

        PointAccount account = new PointAccount();
        account.setUserId(user.getId());
        account.setBalance(0L);
        account.setTotalEarned(0L);
        pointAccountMapper.insert(account);

        return issueTokens(user.getId());
    }

    @Override
    public TokenVO login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BizException(ErrorCode.USER_PASSWORD_WRONG);
        }
        if (user.getStatus() == null || user.getStatus() != UserStatus.ENABLED.getCode()) {
            throw new BizException(ErrorCode.USER_DISABLED);
        }
        return issueTokens(user.getId());
    }

    @Override
    public TokenVO refresh(RefreshTokenRequest request) {
        Claims claims;
        try {
            claims = jwtUtils.parse(request.getRefreshToken());
        } catch (JwtException ex) {
            throw new BizException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
        if (!JwtUtils.TYPE_REFRESH.equals(jwtUtils.type(claims))) {
            throw new BizException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
        Long userId = jwtUtils.userId(claims);
        String stored = redisUtils.get(RedisKey.refreshToken(userId));
        if (!StringUtils.equals(stored, jwtUtils.jti(claims))) {
            throw new BizException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
        return issueTokens(userId);
    }

    @Override
    public void logout(String accessToken) {
        if (StringUtils.isBlank(accessToken)) {
            return;
        }
        try {
            Claims claims = jwtUtils.parse(accessToken);
            String jti = jwtUtils.jti(claims);
            redisUtils.set(RedisKey.tokenBlacklist(jti), "1", jwtUtils.remainingSeconds(claims));
            Long userId = jwtUtils.userId(claims);
            redisUtils.delete(RedisKey.refreshToken(userId));
        } catch (JwtException ignored) {
            // 令牌已无效则视为已登出
        }
    }

    private TokenVO issueTokens(Long userId) {
        String accessJti = IdUtils.uuid();
        String refreshJti = IdUtils.uuid();
        String accessToken = jwtUtils.createAccessToken(userId, accessJti);
        String refreshToken = jwtUtils.createRefreshToken(userId, refreshJti);
        redisUtils.set(RedisKey.refreshToken(userId), refreshJti, jwtUtils.getRefreshExpireSeconds());
        return TokenVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtils.getAccessExpireSeconds())
                .tokenType("Bearer")
                .build();
    }
}
