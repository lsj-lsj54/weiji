package com.weiji.framework.security; // 基础设施包（安全/缓存/Redis）

import io.jsonwebtoken.Claims; // JWT 解析/签发
import io.jsonwebtoken.JwtException; // JWT 解析/签发
import io.jsonwebtoken.Jwts; // JWT 解析/签发
import io.jsonwebtoken.SignatureAlgorithm; // JWT 解析/签发
import io.jsonwebtoken.security.Keys; // JWT 解析/签发
import org.springframework.beans.factory.annotation.Value; // 导入 Value
import org.springframework.stereotype.Component; // 通用组件

import javax.crypto.SecretKey; // 加密
import java.nio.charset.StandardCharsets; // NIO/字符集
import java.util.Date; // JDK 集合/工具

@Component // 交给组件扫描注册
public class JwtUtils { // 定义类 JwtUtils

    public static final String TYPE_ACCESS = "access"; // 必须是 accessToken，refresh 不能当访问令牌
    public static final String TYPE_REFRESH = "refresh"; // 字符串常量

    private final SecretKey key; // 构造注入 key
    private final long accessExpireSeconds; // 构造注入 accessExpireSeconds
    private final long refreshExpireSeconds; // 构造注入 refreshExpireSeconds

    public JwtUtils( // 本行业务语句
            @Value("${weiji.jwt.secret}") String secret, // 本行业务语句
            @Value("${weiji.jwt.access-expire-seconds}") long accessExpireSeconds, // 本行业务语句
            @Value("${weiji.jwt.refresh-expire-seconds}") long refreshExpireSeconds) { // 本行业务语句
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); // 给当前对象赋值
        this.accessExpireSeconds = accessExpireSeconds; // 给当前对象赋值
        this.refreshExpireSeconds = refreshExpireSeconds; // 给当前对象赋值
    }

    public String createAccessToken(Long userId, String jti) { // 方法 createAccessToken
        return build(userId, TYPE_ACCESS, jti, accessExpireSeconds); // 必须是 accessToken，refresh 不能当访问令牌
    }

    public String createRefreshToken(Long userId, String jti) { // 方法 createRefreshToken
        return build(userId, TYPE_REFRESH, jti, refreshExpireSeconds); // 返回结果
    }

    public long getAccessExpireSeconds() { // 方法 getAccessExpireSeconds
        return accessExpireSeconds; // 返回结果
    }

    public long getRefreshExpireSeconds() { // 方法 getRefreshExpireSeconds
        return refreshExpireSeconds; // 返回结果
    }

    public Claims parse(String token) { // 方法 parse
        try { // 捕获异常
            return Jwts.parserBuilder() // 返回结果
                    .setSigningKey(key) // 本行业务语句
                    .build() // 本行业务语句
                    .parseClaimsJws(token) // 本行业务语句
                    .getBody(); // 本行业务语句
        } catch (JwtException | IllegalArgumentException ex) { // 开始代码块
            throw new JwtException("invalid token", ex); // 抛出业务或运行时异常
        }
    }

    public Long userId(Claims claims) { // 方法 userId
        return Long.valueOf(claims.getSubject()); // 返回结果
    }

    public String type(Claims claims) { // 方法 type
        return claims.get("type", String.class); // 返回结果
    }

    public String jti(Claims claims) { // 方法 jti
        return claims.getId(); // 返回结果
    }

    public long remainingSeconds(Claims claims) { // 方法 remainingSeconds
        Date exp = claims.getExpiration(); // 赋值或调用
        long seconds = (exp.getTime() - System.currentTimeMillis()) / 1000; // 赋值或调用
        return Math.max(seconds, 1); // 返回结果
    }

    private String build(Long userId, String type, String jti, long expireSeconds) { // 方法 build
        Date now = new Date(); // 赋值或调用
        Date exp = new Date(now.getTime() + expireSeconds * 1000); // 赋值或调用
        return Jwts.builder() // 返回结果
                .setId(jti) // 本行业务语句
                .setSubject(String.valueOf(userId)) // 本行业务语句
                .claim("type", type) // 本行业务语句
                .setIssuedAt(now) // 本行业务语句
                .setExpiration(exp) // 本行业务语句
                .signWith(key, SignatureAlgorithm.HS256) // 本行业务语句
                .compact(); // 本行业务语句
    }
}
