package icu.yeguo.cloudnest.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.yeguo.cloudnest.model.entity.Setting;
import icu.yeguo.cloudnest.model.vo.SettingVO;
import icu.yeguo.cloudnest.service.ISettingService;
import icu.yeguo.cloudnest.mapper.SettingMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yeguo
 * @description 针对表【cn_setting】的数据库操作Service实现
 * @createDate 2025-01-03 22:15:40
 */
@Service
public class SettingServiceImpl extends ServiceImpl<SettingMapper, Setting> implements ISettingService {

    @Override
    public String getSettingValue(String type, String name) {
        LambdaQueryWrapper<Setting> lambdaQueryWrapper = new LambdaQueryWrapper<Setting>().eq(Setting::getName, name);
        Setting setting = this.getOne(lambdaQueryWrapper);
        return setting.getValue();
    }

    @Override
    public List<SettingVO> getSettingByType(String type) {
        LambdaQueryWrapper<Setting> lambdaQueryWrapper = new LambdaQueryWrapper<Setting>().eq(Setting::getType, type);
        List<Setting> settings = this.list(lambdaQueryWrapper);
        return settings.stream().map(setting -> BeanUtil.toBean(setting, SettingVO.class)).toList();
    }
}