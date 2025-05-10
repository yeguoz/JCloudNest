package icu.yeguo.cloudnest.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import icu.yeguo.cloudnest.exception.BusinessException;
import icu.yeguo.cloudnest.model.vo.UserVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import static icu.yeguo.cloudnest.constant.UserConstant.ADMIN;
import static icu.yeguo.cloudnest.constant.UserConstant.USER_VO;

@Slf4j
@Aspect
@Component
public class UserAuthAspect {
    @Resource
    private HttpSession session;

    @Around("@annotation(icu.yeguo.cloudnest.annotation.UserAuth)")
    public Object userAuthentication(ProceedingJoinPoint joinPoint) throws Throwable {
        Object object = session.getAttribute(USER_VO);
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        UserVO userVO = objectMapper.convertValue(object, UserVO.class);
        String name = userVO.getGroup().getName();
        if (!ADMIN.equals(name)) {
            throw new BusinessException(HttpServletResponse.SC_UNAUTHORIZED, "您没有权限");
        }
        return joinPoint.proceed();
    }
}
