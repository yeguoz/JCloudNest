package icu.yeguo.cloudnest.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.yeguo.cloudnest.exception.BusinessException;
import icu.yeguo.cloudnest.model.entity.Policy;
import icu.yeguo.cloudnest.service.IPolicyService;
import icu.yeguo.cloudnest.mapper.PolicyMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author yeguo
 * @createDate 2025-02-15 18:13:03
 */
@Service
public class PolicyServiceImpl extends ServiceImpl<PolicyMapper, Policy>
        implements IPolicyService {

    @Autowired
    private PolicyMapper policyMapper;

    @Override
    public Policy findPolicyByUserId(int userId) {
        Policy policy;
        policy = policyMapper.findPolicyByUserId(userId);
        if (policy == null)
            throw new BusinessException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "用户策略不存在");
        return policy;
    }
}




