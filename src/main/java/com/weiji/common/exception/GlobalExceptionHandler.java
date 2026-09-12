package com.weiji.common.exception;

import com.weiji.common.enums.ErrorCode;
import com.weiji.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException ex) {
        log.warn("biz error: {}", ex.getMessage());
        return Result.fail(ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValid(Exception ex) {
        String message = ErrorCode.BAD_REQUEST.getMessage();
        if (ex instanceof MethodArgumentNotValidException valid) {
            var fieldError = valid.getBindingResult().getFieldError();
            if (fieldError != null) {
                message = fieldError.getDefaultMessage();
            }
        } else if (ex instanceof BindException bind) {
            var fieldError = bind.getBindingResult().getFieldError();
            if (fieldError != null) {
                message = fieldError.getDefaultMessage();
            }
        }
        return Result.fail(ErrorCode.BAD_REQUEST, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraint(ConstraintViolationException ex) {
        return Result.fail(ErrorCode.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleAuth(AuthenticationException ex) {
        return Result.fail(ErrorCode.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleDenied(AccessDeniedException ex) {
        return Result.fail(ErrorCode.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleOther(Exception ex, HttpServletRequest request) {
        log.error("unhandled error, uri={}", request.getRequestURI(), ex);
        return Result.fail(ErrorCode.INTERNAL_ERROR);
    }
}
