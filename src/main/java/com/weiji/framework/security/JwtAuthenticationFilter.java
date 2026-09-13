package com.weiji.framework.security; // 基础设施包（安全/缓存/Redis）

import com.weiji.common.constant.RedisKey; // 本仓类 RedisKey
import com.weiji.common.enums.ErrorCode; // 本仓类 ErrorCode
import com.weiji.framework.redis.RedisUtils; // 本仓类 RedisUtils
import io.jsonwebtoken.Claims; // JWT 解析/签发
import io.jsonwebtoken.JwtException; // JWT 解析/签发
import jakarta.servlet.FilterChain; // Servlet API
import jakarta.servlet.ServletException; // Servlet API
import jakarta.servlet.http.HttpServletRequest; // Servlet API
import jakarta.servlet.http.HttpServletResponse; // Servlet API
import lombok.RequiredArgsConstructor; // Lombok 样板代码生成
import org.apache.commons.lang3.StringUtils; // 字符串工具
import org.springframework.http.HttpHeaders; // HTTP 类型
import org.springframework.http.MediaType; // HTTP 类型
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Spring Security
import org.springframework.security.core.context.SecurityContextHolder; // Spring Security
import org.springframework.stereotype.Component; // 通用组件
import org.springframework.web.filter.OncePerRequestFilter; // 每请求一次的过滤器

import java.io.IOException; // IO
import java.nio.charset.StandardCharsets; // NIO/字符集
import java.util.List; // JDK 集合/工具

@Component // 交给组件扫描注册
@RequiredArgsConstructor // Lombok：为 final 字段生成构造器注入
public class JwtAuthenticationFilter extends OncePerRequestFilter { // 每个请求只走一次

    private final JwtUtils jwtUtils; // 构造注入 JWT 工具
    private final RedisUtils redisUtils; // 构造注入 Redis 封装

    @Override // 覆盖父类/接口方法
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) // 方法 doFilterInternal
            throws ServletException, IOException { // 开始代码块
        try { // 捕获异常
            String header = request.getHeader(HttpHeaders.AUTHORIZATION); // 读 Authorization 头
            if (StringUtils.startsWithIgnoreCase(header, "Bearer ")) { // 解析 Bearer Token
                String token = header.substring(7).trim(); // 去掉 Bearer 前缀得到 token
                if (StringUtils.isNotBlank(token)) { // token 非空才解析
                    authenticate(token, response); // 校验 JWT 并写入登录态
                    if (response.isCommitted()) { // 若已写出 401 则不再往下走
                        return; // 本行业务语句
                    }
                }
            }
            filterChain.doFilter(request, response); // 放行后续过滤器
        } finally { // 开始代码块
            UserContext.clear(); // 请求结束清理 ThreadLocal，防内存泄漏
        }
    }

    private void authenticate(String token, HttpServletResponse response) throws IOException { // 校验 JWT 并写入登录态
        try { // 捕获异常
            Claims claims = jwtUtils.parse(token); // 签发或解析 JWT
            if (!JwtUtils.TYPE_ACCESS.equals(jwtUtils.type(claims))) { // 必须是 accessToken，refresh 不能当访问令牌
                writeUnauthorized(response, ErrorCode.TOKEN_INVALID); // 写 401 JSON 并中止
                return; // 本行业务语句
            }
            String jti = jwtUtils.jti(claims); // 签发或解析 JWT
            if (Boolean.TRUE.equals(redisUtils.hasKey(RedisKey.tokenBlacklist(jti)))) { // 查 Redis 登出黑名单
                writeUnauthorized(response, ErrorCode.TOKEN_BLACKLISTED); // 写 401 JSON 并中止
                return; // 本行业务语句
            }
            Long userId = jwtUtils.userId(claims); // 签发或解析 JWT
            LoginUser loginUser = new LoginUser(userId, null); // 构造当前用户
            UserContext.set(loginUser); // 写入当前登录用户
            UsernamePasswordAuthenticationToken authentication = // 赋值或调用
                    new UsernamePasswordAuthenticationToken(loginUser, null, List.of()); // Spring Security 认证对象
            SecurityContextHolder.getContext().setAuthentication(authentication); // 写入 Spring Security 认证
        } catch (JwtException ex) { // 开始代码块
            writeUnauthorized(response, ErrorCode.TOKEN_INVALID); // 写 401 JSON 并中止
        }
    }

    private void writeUnauthorized(HttpServletResponse response, ErrorCode errorCode) throws IOException { // 写 401 JSON 并中止
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // HTTP 401
        response.setCharacterEncoding(StandardCharsets.UTF_8.name()); // 响应用 UTF-8
        response.setContentType(MediaType.APPLICATION_JSON_VALUE); // 响应体为 JSON
        String body = "{\"code\":" + errorCode.getCode() + ",\"message\":\"" + errorCode.getMessage() + "\",\"data\":null}"; // 赋值或调用
        response.getWriter().write(body); // 写出 JSON 错误体
    }
}
