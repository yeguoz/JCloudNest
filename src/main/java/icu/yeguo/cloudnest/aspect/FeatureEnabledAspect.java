package icu.yeguo.cloudnest.aspect;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import icu.yeguo.cloudnest.annotation.FeatureEnabled;
import icu.yeguo.cloudnest.constant.HttpStatusConstant;
import icu.yeguo.cloudnest.exception.BusinessException;
import icu.yeguo.cloudnest.model.entity.Setting;
import icu.yeguo.cloudnest.service.ISettingService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static icu.yeguo.cloudnest.constant.SettingConstant.FALSE;

/**
 * 功能开关切面
 */
@Slf4j
@Aspect
@Component
public class FeatureEnabledAspect {

    @Autowired
    private ISettingService iSettingService;

    @Around("@annotation(featureEnabled)")
    public Object checkFeature(ProceedingJoinPoint joinPoint, FeatureEnabled featureEnabled) throws Throwable {
        log.debug("进入功能开关切面");
        String featureName = featureEnabled.featureName();
        log.debug("功能名称：{}", featureName);
        LambdaQueryWrapper<Setting> lambdaQueryWrapper = new LambdaQueryWrapper<Setting>().eq(Setting::getName, featureName);
        Setting setting = iSettingService.getOne(lambdaQueryWrapper);
        log.info("功能开关查询结果：{}", setting);
        String isEnabled = setting.getValue();

        if (isEnabled.equals(FALSE)) {
            log.info("{} is false，功能未开启", featureName);
            throw new BusinessException(HttpStatusConstant.ServiceUnavailable, featureName + "is false");
        }
        log.info("{} is true，功能已经启用", featureName);
        log.debug("功能开关切面结束");
        return joinPoint.proceed();
    }
}
