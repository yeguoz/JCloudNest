package icu.yeguo.cloudnest.controller;

import icu.yeguo.cloudnest.annotation.UserAuth;
import icu.yeguo.cloudnest.common.Response;
import icu.yeguo.cloudnest.model.vo.SettingVO;
import icu.yeguo.cloudnest.service.ISettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static icu.yeguo.cloudnest.constant.SettingConstant.AUTH;

/**
 * @author yeguo
 * @since 2024-12-31
 */
@Tag(name = "设置Controller")
@RestController
@RequestMapping("/setting")
public class SettingController {

    @Resource
    private ISettingService settingService;

    @Operation(summary = "获取Auth配置", responses = {
            @ApiResponse(responseCode = "200", description = "登录成功", content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Response.class)
            )
            )
    })
    @GetMapping("/auth")
    public Response<List<SettingVO>> getAuthSetting() {
        return Response.success(settingService.getSettingByType(AUTH));
    }

    @Operation(summary = "获取类型配置", parameters = {
            @Parameter(name = "type", description = "设置类型", required = true, schema = @Schema(type = "string"))
    }, responses = {
            @ApiResponse(responseCode = "200", description = "登录成功", content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Response.class)
            )
            )
    })
    @UserAuth
    @GetMapping("/type")
    public Response<List<SettingVO>> getSettingByType(@RequestParam(value = "type") String type) {
        return Response.success(settingService.getSettingByType(type));
    }
}
