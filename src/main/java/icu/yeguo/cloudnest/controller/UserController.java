package icu.yeguo.cloudnest.controller;

import icu.yeguo.cloudnest.annotation.FeatureEnabled;
import icu.yeguo.cloudnest.common.Response;
import icu.yeguo.cloudnest.model.dto.UserRegisterDTO;
import icu.yeguo.cloudnest.model.vo.UserVO;
import icu.yeguo.cloudnest.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static icu.yeguo.cloudnest.constant.SettingConstant.REGISTER_ENABLED;

@Tag(name = "用户Controller")
@RestController
@RequestMapping("users")
public class UserController {

    @Autowired
    private IUserService iUserService;

    @FeatureEnabled(featureName = REGISTER_ENABLED)
    @Operation(summary = "创建用户")
    @PostMapping
    public Response<UserVO> createUser(@RequestBody UserRegisterDTO userRegisterDTO) {
        UserVO userVo = iUserService.createUser(userRegisterDTO);
        return Response.success(userVo);
    }

}
