package com.weiji.modules.user.service.impl; // 业务实现

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // MyBatis-Plus
import com.weiji.common.constant.RedisKey; // 本仓类 RedisKey
import com.weiji.common.enums.ErrorCode; // 本仓类 ErrorCode
import com.weiji.common.enums.UserStatus; // 本仓类 UserStatus
import com.weiji.common.exception.BizException; // 本仓类 BizException
import com.weiji.common.utils.IdUtils; // 本仓类 IdUtils
import com.weiji.common.utils.PhoneUtils; // 本仓类 PhoneUtils
import com.weiji.framework.redis.RedisUtils; // 本仓类 RedisUtils
import com.weiji.framework.security.JwtUtils; // 本仓类 JwtUtils
import com.weiji.modules.point.entity.PointAccount; // 本仓类 PointAccount
import com.weiji.modules.point.mapper.PointAccountMapper; // 本仓类 PointAccountMapper
import com.weiji.modules.user.dto.LoginRequest; // 本仓类 LoginRequest
import com.weiji.modules.user.dto.RefreshTokenRequest; // 本仓类 RefreshTokenRequest
import com.weiji.modules.user.dto.RegisterRequest; // 本仓类 RegisterRequest
import com.weiji.modules.user.entity.User; // 本仓类 User
import com.weiji.modules.user.entity.UserPreference; // 本仓类 UserPreference
import com.weiji.modules.user.mapper.UserMapper; // 本仓类 UserMapper
import com.weiji.modules.user.mapper.UserPreferenceMapper; // 本仓类 UserPreferenceMapper
import com.weiji.modules.user.service.AuthService; // 本仓类 AuthService
import com.weiji.modules.user.vo.TokenVO; // 本仓类 TokenVO
import io.jsonwebtoken.Claims; // JWT 解析/签发
import io.jsonwebtoken.JwtException; // JWT 解析/签发
import lombok.RequiredArgsConstructor; // Lombok 样板代码生成
import org.apache.commons.lang3.StringUtils; // 字符串工具
import org.springframework.security.crypto.password.PasswordEncoder; // Spring Security
import org.springframework.stereotype.Service; // 业务层组件
import org.springframework.transaction.annotation.Transactional; // 事务

@Service // 业务层
@RequiredArgsConstructor // Lombok：为 final 字段生成构造器注入
public class AuthServiceImpl implements AuthService { // 定义类 AuthServiceImpl，实现接口

    private final UserMapper userMapper; // 构造注入 userMapper
    private final UserPreferenceMapper userPreferenceMapper; // 构造注入 userPreferenceMapper
    private final PointAccountMapper pointAccountMapper; // 构造注入 pointAccountMapper
    private final PasswordEncoder passwordEncoder; // 校验或加密密码
    private final JwtUtils jwtUtils; // 构造注入 JWT 工具
    private final RedisUtils redisUtils; // 构造注入 Redis 封装

    @Override // 覆盖父类/接口方法
    @Transactional // 声明式事务，异常则回滚
    public TokenVO register(RegisterRequest request) { // 方法 register
        if (!PhoneUtils.isMobile(request.getPhone())) { // 条件判断
            throw new BizException(ErrorCode.BAD_REQUEST, "手机号格式不正确"); // 抛出业务或运行时异常
        }
        long exists = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone())); // 计数
        if (exists > 0) { // 条件判断
            throw new BizException(ErrorCode.USER_PHONE_EXISTS); // 抛出业务或运行时异常
        }
        User user = new User(); // 赋值或调用
        user.setPhone(request.getPhone()); // 本行业务语句
        user.setPasswordHash(passwordEncoder.encode(request.getPassword())); // 校验或加密密码
        user.setNickname(StringUtils.defaultIfBlank(request.getNickname(), "微积用户" + request.getPhone().substring(7))); // 去掉 Bearer 前缀得到 token
        user.setStatus(UserStatus.ENABLED.getCode()); // 本行业务语句
        user.setDeleted(0); // 本行业务语句
        userMapper.insert(user); // 插入一行

        UserPreference preference = new UserPreference(); // 赋值或调用
        preference.setUserId(user.getId()); // 本行业务语句
        preference.setEnergyStatus("ENERGETIC"); // 本行业务语句
        userPreferenceMapper.insert(preference); // 插入一行

        PointAccount account = new PointAccount(); // 赋值或调用
        account.setUserId(user.getId()); // 本行业务语句
        account.setBalance(0L); // 本行业务语句
        account.setTotalEarned(0L); // 本行业务语句
        pointAccountMapper.insert(account); // 插入一行

        return issueTokens(user.getId()); // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public TokenVO login(LoginRequest request) { // 方法 login
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone())); // 查一条
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) { // 校验或加密密码
            throw new BizException(ErrorCode.USER_PASSWORD_WRONG); // 抛出业务或运行时异常
        }
        if (user.getStatus() == null || user.getStatus() != UserStatus.ENABLED.getCode()) { // 条件判断
            throw new BizException(ErrorCode.USER_DISABLED); // 抛出业务或运行时异常
        }
        return issueTokens(user.getId()); // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public TokenVO refresh(RefreshTokenRequest request) { // 方法 refresh
        Claims claims; // 本行业务语句
        try { // 捕获异常
            claims = jwtUtils.parse(request.getRefreshToken()); // 签发或解析 JWT
        } catch (JwtException ex) { // 开始代码块
            throw new BizException(ErrorCode.REFRESH_TOKEN_INVALID); // 令牌无效或过期
        }
        if (!JwtUtils.TYPE_REFRESH.equals(jwtUtils.type(claims))) { // 签发或解析 JWT
            throw new BizException(ErrorCode.REFRESH_TOKEN_INVALID); // 令牌无效或过期
        }
        Long userId = jwtUtils.userId(claims); // 签发或解析 JWT
        String stored = redisUtils.get(RedisKey.refreshToken(userId)); // 调用 Redis 封装
        if (!StringUtils.equals(stored, jwtUtils.jti(claims))) { // 签发或解析 JWT
            throw new BizException(ErrorCode.REFRESH_TOKEN_INVALID); // 令牌无效或过期
        }
        return issueTokens(userId); // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public void logout(String accessToken) { // 方法 logout
        if (StringUtils.isBlank(accessToken)) { // 条件判断
            return; // 本行业务语句
        }
        try { // 捕获异常
            Claims claims = jwtUtils.parse(accessToken); // 签发或解析 JWT
            String jti = jwtUtils.jti(claims); // 签发或解析 JWT
            redisUtils.set(RedisKey.tokenBlacklist(jti), "1", jwtUtils.remainingSeconds(claims)); // 查 Redis 登出黑名单
            Long userId = jwtUtils.userId(claims); // 签发或解析 JWT
            redisUtils.delete(RedisKey.refreshToken(userId)); // 调用 Redis 封装
        } catch (JwtException ignored) { // 开始代码块
            // 令牌已无效则视为已登出
        }
    }

    private TokenVO issueTokens(Long userId) { // 方法 issueTokens
        String accessJti = IdUtils.uuid(); // 赋值或调用
        String refreshJti = IdUtils.uuid(); // 赋值或调用
        String accessToken = jwtUtils.createAccessToken(userId, accessJti); // 签发或解析 JWT
        String refreshToken = jwtUtils.createRefreshToken(userId, refreshJti); // 签发或解析 JWT
        redisUtils.set(RedisKey.refreshToken(userId), refreshJti, jwtUtils.getRefreshExpireSeconds()); // 调用 Redis 封装
        return TokenVO.builder() // 返回结果
                .accessToken(accessToken) // 本行业务语句
                .refreshToken(refreshToken) // 本行业务语句
                .expiresIn(jwtUtils.getAccessExpireSeconds()) // 签发或解析 JWT
                .tokenType("Bearer") // 本行业务语句
                .build(); // 本行业务语句
    }
}
