package icu.yeguo.cloudnest.aspect;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import icu.yeguo.cloudnest.annotation.FeatureEnabled;
import icu.yeguo.cloudnest.exception.BusinessException;
import icu.yeguo.cloudnest.model.entity.Setting;
import icu.yeguo.cloudnest.service.ISettingService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import static icu.yeguo.cloudnest.constant.CommonConstant.FALSE;

/**
 * 功能开关切面
 */
@Slf4j
@Aspect
@Component
public class FeatureEnabledAspect {

    @Resource
    private ISettingService iSettingService;

    @Around("@annotation(featureEnabled)")
    public Object checkFeature(ProceedingJoinPoint joinPoint, FeatureEnabled featureEnabled) throws Throwable {
        String featureName = featureEnabled.featureName();
        LambdaQueryWrapper<Setting> lambdaQueryWrapper = new LambdaQueryWrapper<Setting>().eq(Setting::getName, featureName);
        Setting setting = iSettingService.getOne(lambdaQueryWrapper);
        String isEnabled = setting.getValue();

        if (isEnabled.equals(FALSE)) {
            throw new BusinessException(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "该服务未开启");
        }
        return joinPoint.proceed();
    }
}