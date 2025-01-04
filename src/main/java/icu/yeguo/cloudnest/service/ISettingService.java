package icu.yeguo.cloudnest.service;

import icu.yeguo.cloudnest.model.entity.Setting;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author yeguo
 * @description 针对表【cn_setting】的数据库操作Service
 * @createDate 2025-01-03 22:15:40
 */
public interface ISettingService extends IService<Setting> {
    String getSettingValue(String name);
}
