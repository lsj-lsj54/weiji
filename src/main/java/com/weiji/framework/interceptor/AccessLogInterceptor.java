package com.weiji.framework.interceptor; // 基础设施包（安全/缓存/Redis）

import com.weiji.framework.security.UserContext; // 本仓类 UserContext
import jakarta.servlet.http.HttpServletRequest; // Servlet API
import jakarta.servlet.http.HttpServletResponse; // Servlet API
import lombok.extern.slf4j.Slf4j; // Lombok 样板代码生成
import org.springframework.stereotype.Component; // 通用组件
import org.springframework.web.servlet.HandlerInterceptor; // Spring MVC

@Slf4j // Lombok 生成 log，走 Logback
@Component // 交给组件扫描注册
public class AccessLogInterceptor implements HandlerInterceptor { // 定义类 AccessLogInterceptor，实现接口

    private static final String START_AT = "weiji.access.start"; // 字符串常量

    @Override // 覆盖父类/接口方法
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) { // 方法 preHandle
        request.setAttribute(START_AT, System.currentTimeMillis()); // 本行业务语句
        return true; // 返回 true
    }

    @Override // 覆盖父类/接口方法
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) { // 方法 afterCompletion
        Object start = request.getAttribute(START_AT); // 赋值或调用
        long cost = start instanceof Long startAt ? System.currentTimeMillis() - startAt : -1; // 赋值或调用
        log.info("access method={} uri={} status={} userId={} costMs={}", // 赋值或调用
                request.getMethod(), // 本行业务语句
                request.getRequestURI(), // 本行业务语句
                response.getStatus(), // 本行业务语句
                UserContext.userId(), // 本行业务语句
                cost); // 本行业务语句
    }
}
