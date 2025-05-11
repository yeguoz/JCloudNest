package icu.yeguo.cloudnest.exception;

import icu.yeguo.cloudnest.common.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(BusinessException.class)
    public Response<?> businessExceptionHandler(BusinessException e) {
        log.error("业务异常-->{}类:{}:{}",e.getClass(),e.getMessage(),e.getStackTrace());
        return Response.error(e.getCode(),e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Response<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("运行时异常-->{}类:{}:{}",e.getClass(),e.getMessage(),e.getStackTrace());
        return Response.error(e.getMessage());
    }
}
