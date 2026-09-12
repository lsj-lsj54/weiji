package com.weiji.framework.interceptor;

import com.weiji.framework.security.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class AccessLogInterceptor implements HandlerInterceptor {

    private static final String START_AT = "weiji.access.start";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_AT, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Object start = request.getAttribute(START_AT);
        long cost = start instanceof Long startAt ? System.currentTimeMillis() - startAt : -1;
        log.info("access method={} uri={} status={} userId={} costMs={}",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                UserContext.userId(),
                cost);
    }
}
