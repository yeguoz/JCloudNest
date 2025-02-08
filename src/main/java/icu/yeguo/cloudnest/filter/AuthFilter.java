package icu.yeguo.cloudnest.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import icu.yeguo.cloudnest.common.Response;
import icu.yeguo.cloudnest.util.PatchMatchUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static icu.yeguo.cloudnest.constant.CommonConstant.APPLICATION_JSON_UTF8_VALUE;
import static icu.yeguo.cloudnest.constant.UserConstant.USER_VO;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@Slf4j
@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        HttpSession session = httpServletRequest.getSession();
        String requestURI = httpServletRequest.getRequestURI();
        log.info("当前请求的 URI: {}", requestURI);

        if (!isExcludeItem(requestURI)) {
            log.debug("进行身份校验...");
            log.debug("UserVO: {}", session.getAttribute(USER_VO));
            if (session.getAttribute(USER_VO) == null) {
                Response<Object> result = Response.error(SC_UNAUTHORIZED, "未登录");
                // 设置响应为 JSON 格式
                httpServletResponse.setStatus(SC_OK);
                httpServletResponse.setContentType(APPLICATION_JSON_UTF8_VALUE);
                // 将结果转换为 JSON 字符串
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonResponse = objectMapper.writeValueAsString(result);
                // 响应
                PrintWriter writer = httpServletResponse.getWriter();
                writer.write(jsonResponse);
                writer.flush();
                return;
            }
            log.debug("身份验证通过...");
        }
        chain.doFilter(request, response);
    }

    public static boolean isExcludeItem(String requestURI) {
        List<String> excludePaths = new ArrayList<>();
        // swagger
        excludePaths.add("/api/doc.html");
        excludePaths.add("/api/v3/**");
        excludePaths.add("/api/webjars/**");
        // other
        excludePaths.add("/api/users");
        excludePaths.add("/api/users/current");
        excludePaths.add("/api/users/auth/login");
        excludePaths.add("/api/users/captcha");
        excludePaths.add("/api/setting/auth");

        for (String excludePath : excludePaths) {
            if (PatchMatchUtil.isPathMatch(requestURI, excludePath)) {
                return true;
            }
        }
        return false;
    }

}