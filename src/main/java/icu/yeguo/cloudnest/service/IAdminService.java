package icu.yeguo.cloudnest.service;

public interface IAdminService {
    Boolean editUser(Integer id, Integer groupId, Integer policyId, Byte status);
}