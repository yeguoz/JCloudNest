package icu.yeguo.cloudnest.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private GlobalInterceptor globalInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 全局拦截器，拦截所有请求
        registry.addInterceptor(globalInterceptor)
                .addPathPatterns("/**")
                // swagger
                .excludePathPatterns("/doc.html")
                .excludePathPatterns("/v3/**")
                .excludePathPatterns("/webjars/**")
                // other
                .excludePathPatterns("/users")
                .excludePathPatterns("/users/current")
                .excludePathPatterns("/users/auth/login")
                .excludePathPatterns("/users/captcha")
                .excludePathPatterns("/users/verify/*")
                .excludePathPatterns("/users/forget")
                .excludePathPatterns("/users/reset/pwd")
                .excludePathPatterns("/users/avatar")
                .excludePathPatterns("/setting/auth")
                // storage
                .excludePathPatterns("/storage/**")
                // share
                .excludePathPatterns("/s/**");
    }
}