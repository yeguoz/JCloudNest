package icu.yeguo.cloudnest.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import icu.yeguo.cloudnest.annotation.UserAuth;
import icu.yeguo.cloudnest.common.Response;
import icu.yeguo.cloudnest.constant.UserConstant;
import icu.yeguo.cloudnest.exception.BusinessException;
import icu.yeguo.cloudnest.model.dto.AuthInfoDTO;
import icu.yeguo.cloudnest.model.dto.MailInfoDTO;
import icu.yeguo.cloudnest.model.dto.SettingItem;
import icu.yeguo.cloudnest.model.dto.SiteInfoDTO;
import icu.yeguo.cloudnest.model.entity.*;
import icu.yeguo.cloudnest.model.vo.*;
import icu.yeguo.cloudnest.service.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Resource
    private IFileService fileService;
    @Resource
    private IShareService shareService;
    @Resource
    private IUserService userService;
    @Resource
    private IUserFileService userFileService;
    @Resource
    private IGroupService groupService;
    @Resource
    private IPolicyService policyService;
    @Resource
    private IAdminService adminService;
    @Resource
    private ISettingService settingService;

    @UserAuth
    @GetMapping("/files")
    public Response<List<AdminFileVO>> getFiles() {
        BaseMapper<File> baseMapper = fileService.getBaseMapper();
        List<File> files = baseMapper.selectList(new LambdaQueryWrapper<>());
        List<AdminFileVO> list = files.stream().map(f -> BeanUtil.toBean(f, AdminFileVO.class)).toList();
        return Response.success(list);
    }

    @UserAuth
    @GetMapping("/share")
    public Response<List<AdminShareFileVO>> getSharedFilesByAdmin() {
        List<AdminShareFileVO> list = shareService.getSharedFilesByAdmin();
        return Response.success(list);
    }

    @UserAuth
    @GetMapping("/users")
    public Response<List<AdminUserVO>> getUsersByAdmin() {
        List<AdminUserVO> users = userService.getUsersByAdmin();
        return Response.success(users);
    }

    @UserAuth
    @GetMapping("/policies")
    public Response<List<PolicyVO>> getPolicies() {
        List<Policy> policies = policyService.getBaseMapper().selectList(new LambdaQueryWrapper<>());
        List<PolicyVO> list = policies.stream().map(p -> BeanUtil.toBean(p, PolicyVO.class)).toList();
        return Response.success(list);
    }

    @UserAuth
    @GetMapping("/userfiles")
    public Response<List<AdminUserFileVO>> getUserFilesByAdmin() {
        List<AdminUserFileVO> list = userFileService.getUserFilesByAdmin();
        return Response.success(list);
    }

    @UserAuth
    @DeleteMapping("/share")
    public Response<?> deleteSharedFile(@RequestParam("id") Integer id) {
        shareService.removeById(id);
        return Response.success(null, "删除成功");
    }

    @UserAuth
    @DeleteMapping("/userfile")
    public Response<?> deleteUserFile(@RequestParam("id") Integer id) {
        userFileService.removeById(id);
        return Response.success(null, "删除成功");
    }

    @UserAuth
    @GetMapping("/groups")
    public Response<?> getGroups() {
        List<Group> groups = groupService.getBaseMapper().selectList(new LambdaQueryWrapper<>());
        return Response.success(groups);
    }

    @UserAuth
    @DeleteMapping("/user")
    public Response<?> deleteUser(@RequestParam("id") Integer id) {
        userService.removeById(id);
        return Response.success(null, "删除成功");
    }

    @UserAuth
    @DeleteMapping("/group")
    public Response<?> deleteGroup(@RequestParam("id") Integer id) {
        Group one = groupService.getOne(new LambdaQueryWrapper<Group>().eq(Group::getId, id));
        if (one.getName().equals(UserConstant.ADMIN) || one.getName().equals(UserConstant.USER)) {
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "不能删除系统组");
        }
        groupService.removeById(id);
        return Response.success(null, "删除成功");
    }

    @UserAuth
    @PostMapping("/group")
    public Response<?> createGroup(@RequestParam("groupName") String groupName,
                                   @RequestParam("policyId") Integer policyId,
                                   @RequestParam("maxStorage") Long maxStorage,
                                   @RequestParam("shareEnabled") Boolean shareEnabled) {
        if (groupService.getOne(new LambdaQueryWrapper<Group>().eq(Group::getName, groupName)) != null) {
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "组名已存在");
        }
        if (groupName.length() < 4 || groupName.length() > 10) {
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "组名长度不合法");
        }
        Group group = new Group();
        group.setName(groupName);
        group.setPolicyId(policyId);
        group.setMaxStorage(maxStorage);
        group.setShareEnabled(shareEnabled ? (byte) 1 : (byte) 0);
        groupService.save(group);
        return Response.success(null, "创建成功");
    }

    @UserAuth
    @PutMapping("/group")
    public Response<?> editGroup(@RequestParam("id") Integer id,
                                 @RequestParam("groupName") String groupName,
                                 @RequestParam("policyId") Integer policyId,
                                 @RequestParam("maxStorage") Long maxStorage,
                                 @RequestParam("shareEnabled") Boolean shareEnabled) {
        if (groupService.getById(id).getName().equals(groupName)) {
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "组名已存在");
        }
        if (groupName.length() < 4 || groupName.length() > 10) {
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "组名长度不合法");
        }
        groupService.lambdaUpdate()
                .eq(Group::getId, id)
                .set(Group::getName, groupName)
                .set(Group::getPolicyId, policyId)
                .set(Group::getMaxStorage, maxStorage)
                .set(Group::getShareEnabled, shareEnabled ? (byte) 1 : (byte) 0)
                .update();
        return Response.success(null, "修改成功");
    }

    @UserAuth
    @PutMapping("/user")
    public Response<?> editUser(@RequestParam("id") Integer id,
                                @RequestParam("groupId") Integer groupId,
                                @RequestParam("policyId") Integer policyId,
                                @RequestParam("status") Byte status) {
        adminService.editUser(id, groupId, policyId, status);
        return Response.success(null, "修改成功");
    }

    @UserAuth
    @PutMapping("/site")
    public Response<?> editSiteInfo(@RequestBody SiteInfoDTO siteInfoDTO) {
        return updateSettings(siteInfoDTO.getAllSettings());
    }

    @Transactional
    @UserAuth
    @PutMapping("/auth")
    public Response<?> editAuthInfo(@RequestBody AuthInfoDTO authInfoDTO) {
        return updateSettings(authInfoDTO.getAllSettings());
    }

    @Transactional
    @UserAuth
    @PutMapping("/mail")
    public Response<?> editMailInfo(@RequestBody MailInfoDTO mailInfoDTO) {
        return updateSettings(mailInfoDTO.getAllSettings());
    }

    private Response<?> updateSettings(List<SettingItem> settings) {
        settings.stream()
                .filter(Objects::nonNull)
                .forEach(this::updateSetting);
        return Response.success(null, "修改成功");
    }

    private void updateSetting(SettingItem item) {
        settingService.lambdaUpdate()
                .eq(Setting::getId, item.getId())
                .set(Setting::getValue, item.getValue())
                .update();
    }
}
