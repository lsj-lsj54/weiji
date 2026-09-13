package com.weiji.common.exception; // 公共包（结果/异常/常量）

import com.weiji.common.enums.ErrorCode; // 本仓类 ErrorCode
import com.weiji.common.result.Result; // 本仓类 Result
import jakarta.servlet.http.HttpServletRequest; // Servlet API
import jakarta.validation.ConstraintViolationException; // Bean Validation
import lombok.extern.slf4j.Slf4j; // Lombok 样板代码生成
import org.springframework.http.HttpStatus; // HTTP 类型
import org.springframework.security.access.AccessDeniedException; // Spring Security
import org.springframework.security.core.AuthenticationException; // Spring Security
import org.springframework.validation.BindException; // 参数校验
import org.springframework.web.bind.MethodArgumentNotValidException; // 导入 MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler; // Web 映射注解
import org.springframework.web.bind.annotation.ResponseStatus; // Web 映射注解
import org.springframework.web.bind.annotation.RestControllerAdvice; // Web 映射注解

@Slf4j // Lombok 生成 log，走 Logback
@RestControllerAdvice // REST 控制器，返回值写入 HTTP 体
public class GlobalExceptionHandler { // 定义类 GlobalExceptionHandler

    @ExceptionHandler(BizException.class) // 本行业务语句
    public Result<Void> handleBiz(BizException ex) { // 方法 handleBiz
        log.warn("biz error: {}", ex.getMessage()); // 本行业务语句
        return Result.fail(ex.getErrorCode(), ex.getMessage()); // 失败响应
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class}) // 本行业务语句
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 本行业务语句
    public Result<Void> handleValid(Exception ex) { // 方法 handleValid
        String message = ErrorCode.BAD_REQUEST.getMessage(); // 赋值或调用
        if (ex instanceof MethodArgumentNotValidException valid) { // 条件判断
            var fieldError = valid.getBindingResult().getFieldError(); // 赋值或调用
            if (fieldError != null) { // 条件判断
                message = fieldError.getDefaultMessage(); // 赋值或调用
            }
        } else if (ex instanceof BindException bind) { // 开始代码块
            var fieldError = bind.getBindingResult().getFieldError(); // 赋值或调用
            if (fieldError != null) { // 条件判断
                message = fieldError.getDefaultMessage(); // 赋值或调用
            }
        }
        return Result.fail(ErrorCode.BAD_REQUEST, message); // 失败响应
    }

    @ExceptionHandler(ConstraintViolationException.class) // 本行业务语句
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 本行业务语句
    public Result<Void> handleConstraint(ConstraintViolationException ex) { // 方法 handleConstraint
        return Result.fail(ErrorCode.BAD_REQUEST, ex.getMessage()); // 失败响应
    }

    @ExceptionHandler(AuthenticationException.class) // 本行业务语句
    @ResponseStatus(HttpStatus.UNAUTHORIZED) // 本行业务语句
    public Result<Void> handleAuth(AuthenticationException ex) { // 方法 handleAuth
        return Result.fail(ErrorCode.UNAUTHORIZED); // 失败响应
    }

    @ExceptionHandler(AccessDeniedException.class) // 本行业务语句
    @ResponseStatus(HttpStatus.FORBIDDEN) // 本行业务语句
    public Result<Void> handleDenied(AccessDeniedException ex) { // 方法 handleDenied
        return Result.fail(ErrorCode.FORBIDDEN); // 失败响应
    }

    @ExceptionHandler(Exception.class) // 本行业务语句
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 本行业务语句
    public Result<Void> handleOther(Exception ex, HttpServletRequest request) { // 方法 handleOther
        log.error("unhandled error, uri={}", request.getRequestURI(), ex); // 赋值或调用
        return Result.fail(ErrorCode.INTERNAL_ERROR); // 失败响应
    }
}
