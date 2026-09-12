package com.weiji.framework.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final long accessExpireSeconds;
    private final long refreshExpireSeconds;

    public JwtUtils(
            @Value("${weiji.jwt.secret}") String secret,
            @Value("${weiji.jwt.access-expire-seconds}") long accessExpireSeconds,
            @Value("${weiji.jwt.refresh-expire-seconds}") long refreshExpireSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpireSeconds = accessExpireSeconds;
        this.refreshExpireSeconds = refreshExpireSeconds;
    }

    public String createAccessToken(Long userId, String jti) {
        return build(userId, TYPE_ACCESS, jti, accessExpireSeconds);
    }

    public String createRefreshToken(Long userId, String jti) {
        return build(userId, TYPE_REFRESH, jti, refreshExpireSeconds);
    }

    public long getAccessExpireSeconds() {
        return accessExpireSeconds;
    }

    public long getRefreshExpireSeconds() {
        return refreshExpireSeconds;
    }

    public Claims parse(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException ex) {
            throw new JwtException("invalid token", ex);
        }
    }

    public Long userId(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }

    public String type(Claims claims) {
        return claims.get("type", String.class);
    }

    public String jti(Claims claims) {
        return claims.getId();
    }

    public long remainingSeconds(Claims claims) {
        Date exp = claims.getExpiration();
        long seconds = (exp.getTime() - System.currentTimeMillis()) / 1000;
        return Math.max(seconds, 1);
    }

    private String build(Long userId, String type, String jti, long expireSeconds) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expireSeconds * 1000);
        return Jwts.builder()
                .setId(jti)
                .setSubject(String.valueOf(userId))
                .claim("type", type)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
