package icu.yeguo.cloudnest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import icu.yeguo.cloudnest.annotation.FeatureEnabled;
import icu.yeguo.cloudnest.common.Response;
import icu.yeguo.cloudnest.exception.BusinessException;
import icu.yeguo.cloudnest.model.dto.UserLoginDTO;
import icu.yeguo.cloudnest.model.dto.UserRegisterDTO;
import icu.yeguo.cloudnest.model.vo.UserVO;
import icu.yeguo.cloudnest.service.IUserService;
import icu.yeguo.cloudnest.util.CaptchaUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static icu.yeguo.cloudnest.constant.CommonConstant.*;
import static icu.yeguo.cloudnest.constant.SettingConstant.REGISTER_ENABLED;
import static icu.yeguo.cloudnest.constant.UserConstant.USER_VO;

@Tag(name = "用户Controller")
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private IUserService userService;

    @FeatureEnabled(featureName = REGISTER_ENABLED)
    @Operation(summary = "创建用户", parameters = {
            @Parameter(name = CAPTCHA, description = "验证码", schema = @Schema(type = "string"))
    },
            responses = {
                    @ApiResponse(responseCode = "200", description = "创建用户成功", content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Response.class)
                    )
                    )
            })
    @PostMapping
    public Response<UserVO> createUser(HttpSession session,
                                       @RequestBody UserRegisterDTO userRegisterDTO,
                                       @RequestParam(value = CAPTCHA, required = false) String captcha) {
        UserVO userVo = userService.createUser(session, userRegisterDTO, captcha);
        return Response.success(userVo);
    }

    @Operation(summary = "用户登录", responses = {
            @ApiResponse(responseCode = "200", description = "登录成功", content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Response.class)
            )
            )
    })
    @PostMapping("/auth/login")
    public Response<UserVO> login(HttpSession session,
                                  @RequestBody UserLoginDTO userLoginDTO,
                                  @Parameter(description = "验证码")
                                  @RequestParam(value = CAPTCHA, required = false) String captcha) {
        UserVO userVO = userService.login(session, userLoginDTO, captcha);
        return Response.success(userVO);
    }

    @Operation(summary = "获取验证码", responses = {
            @ApiResponse(responseCode = "200", description = "登录成功", content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Response.class)
            )
            )
    })
    @GetMapping("/captcha")
    public Response<String> getCaptcha(HttpServletRequest request, String captchaType) throws IOException {
        String captchaMaxAge = SESSION_CAPTCHA_LOGIN.equals(captchaType)
                ? SESSION_CAPTCHA_LOGIN_MAX_AGE : SESSION_CAPTCHA_REGISTER_MAX_AGE;
        String captchaText = CaptchaUtil.generateCaptchaText(5);
        BufferedImage image = CaptchaUtil.generateCaptchaImage(captchaText);
        String base64 = CaptchaUtil.generateCaptchaBase64(image);
        // 将验证码内容存储到 Redis 或 Session 中
        HttpSession session = request.getSession();
        session.setAttribute(captchaType, captchaText);
        session.setAttribute(captchaMaxAge, System.currentTimeMillis() + 5 * 60 * 1000);
        return Response.success(BASE64_IMAGE_PREFIX + base64);
    }

    @Operation(summary = "获取登录用户", responses = {
            @ApiResponse(responseCode = "200", description = "获取用户成功", content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Response.class)
            ))}
    )
    @GetMapping("/current")
    public Response<UserVO> getCurrentUser(HttpServletRequest request) {
        Object object = request.getSession().getAttribute(USER_VO);
        if (object == null)
            throw new BusinessException(HttpServletResponse.SC_UNAUTHORIZED, "未登录");
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        UserVO userVO = objectMapper.convertValue(object, UserVO.class);
        return Response.success(userVO);
    }


    @Operation(summary = "注销", responses = {
            @ApiResponse(responseCode = "200", description = "注销成功", content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Response.class)
            ))}
    )
    @PostMapping("/logout")
    public Response<Void> logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return Response.success(null);
    }
}

