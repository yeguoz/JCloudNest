package icu.yeguo.cloudnest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import icu.yeguo.cloudnest.exception.BusinessException;
import icu.yeguo.cloudnest.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static icu.yeguo.cloudnest.constant.UserConstant.USER_VO;

@Slf4j
@Component
public class GlobalInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.debug("GlobalInterceptor处理路径:{}", request.getRequestURI());
        log.debug("进行身份校验...");
        HttpSession session = request.getSession();
        Object object = session.getAttribute(USER_VO);
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        UserVO userVO = objectMapper.convertValue(object, UserVO.class);

        if (userVO == null)
            throw new BusinessException(HttpServletResponse.SC_UNAUTHORIZED, "未登录");

        log.debug("身份验证通过");
        return true;
    }
}
