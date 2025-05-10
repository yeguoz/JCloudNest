package icu.yeguo.cloudnest.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import icu.yeguo.cloudnest.exception.BusinessException;
import icu.yeguo.cloudnest.model.vo.UserVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import static icu.yeguo.cloudnest.constant.UserConstant.USER_VO;

@Slf4j
@Aspect
@Component
public class SessionUserAspect {

    @Resource
    private HttpServletRequest request;

    @Around("@annotation(icu.yeguo.cloudnest.annotation.SessionUser)")
    public Object injectUserVO(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpSession session = request.getSession(false);
        if (session == null)
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "未登录");

        Object object = session.getAttribute(USER_VO);
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        UserVO userVO = objectMapper.convertValue(object, UserVO.class);

        if (userVO == null)
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "未登录");

        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof UserVO) {
                args[i] = userVO;
                break;
            }
        }
        return joinPoint.proceed(args);
    }
}
