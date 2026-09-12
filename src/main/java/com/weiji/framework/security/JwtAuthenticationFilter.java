package com.weiji.framework.security;

import com.weiji.common.constant.RedisKey;
import com.weiji.common.enums.ErrorCode;
import com.weiji.framework.redis.RedisUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final RedisUtils redisUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (StringUtils.startsWithIgnoreCase(header, "Bearer ")) {
                String token = header.substring(7).trim();
                if (StringUtils.isNotBlank(token)) {
                    authenticate(token, response);
                    if (response.isCommitted()) {
                        return;
                    }
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }

    private void authenticate(String token, HttpServletResponse response) throws IOException {
        try {
            Claims claims = jwtUtils.parse(token);
            if (!JwtUtils.TYPE_ACCESS.equals(jwtUtils.type(claims))) {
                writeUnauthorized(response, ErrorCode.TOKEN_INVALID);
                return;
            }
            String jti = jwtUtils.jti(claims);
            if (Boolean.TRUE.equals(redisUtils.hasKey(RedisKey.tokenBlacklist(jti)))) {
                writeUnauthorized(response, ErrorCode.TOKEN_BLACKLISTED);
                return;
            }
            Long userId = jwtUtils.userId(claims);
            LoginUser loginUser = new LoginUser(userId, null);
            UserContext.set(loginUser);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(loginUser, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException ex) {
            writeUnauthorized(response, ErrorCode.TOKEN_INVALID);
        }
    }

    private void writeUnauthorized(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String body = "{\"code\":" + errorCode.getCode() + ",\"message\":\"" + errorCode.getMessage() + "\",\"data\":null}";
        response.getWriter().write(body);
    }
}
