package icu.yeguo.cloudnest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.yeguo.cloudnest.model.entity.Policy;

/**
* @author yeguo
* @createDate 2025-02-15 18:13:03
*/
public interface IPolicyService extends IService<Policy> {
    Policy findPolicyByUserId(int userId);
}
