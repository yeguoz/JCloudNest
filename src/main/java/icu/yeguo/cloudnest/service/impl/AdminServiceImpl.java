package icu.yeguo.cloudnest.service.impl;

import icu.yeguo.cloudnest.model.entity.Group;
import icu.yeguo.cloudnest.model.entity.User;
import icu.yeguo.cloudnest.service.IAdminService;
import icu.yeguo.cloudnest.service.IGroupService;
import icu.yeguo.cloudnest.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminServiceImpl implements IAdminService {

    @Resource
    private IUserService userService;
    @Resource
    private IGroupService groupService;

    @Transactional
    @Override
    public Boolean editUser(Integer id, Integer groupId, Integer policyId, Byte status) {
        userService.lambdaUpdate()
                .eq(User::getId, id)
                .set(User::getGroupId, groupId)
                .set(User::getStatus, status)
                .update();
        groupService.lambdaUpdate()
                .eq(Group::getId, groupId)
                .set(Group::getPolicyId, policyId)
                .update();
        return true;
    }
}