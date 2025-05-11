package icu.yeguo.cloudnest.controller;

import at.favre.lib.crypto.bcrypt.BCrypt;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import icu.yeguo.cloudnest.annotation.FeatureEnabled;
import icu.yeguo.cloudnest.annotation.SessionUser;
import icu.yeguo.cloudnest.common.Response;
import icu.yeguo.cloudnest.exception.BusinessException;
import icu.yeguo.cloudnest.model.dto.ForgetPwdDTO;
import icu.yeguo.cloudnest.model.dto.UserLoginDTO;
import icu.yeguo.cloudnest.model.dto.UserRegisterDTO;
import icu.yeguo.cloudnest.model.entity.User;
import icu.yeguo.cloudnest.model.vo.UserVO;
import icu.yeguo.cloudnest.service.IUserService;
import icu.yeguo.cloudnest.util.CaptchaUtils;
import icu.yeguo.cloudnest.util.FileHandlerUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static icu.yeguo.cloudnest.constant.CommonConstant.*;
import static icu.yeguo.cloudnest.constant.PlaceholderConstant.*;
import static icu.yeguo.cloudnest.constant.SettingConstant.REGISTER_ENABLED;
import static icu.yeguo.cloudnest.constant.UserConstant.NORMAL;
import static icu.yeguo.cloudnest.constant.UserConstant.USER_VO;
import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

@Slf4j
@Tag(name = "用户Controller")
@RestController
@RequestMapping("/users")
public class UserController {

    @Resource
    private IUserService userService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

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
        String captchaText = CaptchaUtils.generateCaptchaText(5);
        BufferedImage image = CaptchaUtils.generateCaptchaImage(captchaText);
        String base64 = CaptchaUtils.generateCaptchaBase64(image);
        HttpSession session = request.getSession();
        session.setAttribute(captchaType, captchaText);
        session.setAttribute(captchaMaxAge, System.currentTimeMillis() + 5 * 60 * 1000);
        return Response.success(BASE64_IMAGE_PREFIX + base64);
    }

    @PutMapping("/verify/register")
    public Response<Integer> emailVerify(@RequestParam("id") Integer id,
                                         @RequestParam("token") String token) {

        String t = stringRedisTemplate.opsForValue().get("register:" + id);
        if (!token.equals(t))
            throw new BusinessException(SC_BAD_REQUEST, "验证失败");
        userService.lambdaUpdate().eq(User::getId, id).set(User::getStatus, NORMAL).update();
        stringRedisTemplate.delete("register:" + id);
        return Response.success(id, "成功");
    }

    @PostMapping("/forget")
    public Response<String> forgetPwd(@RequestParam("email") String email) {
        userService.forgetPwd(email);
        return Response.success(email);
    }

    @PutMapping("/reset/pwd")
    public Response<Integer> resetPwd(@RequestParam("email") String email,
                                      @RequestParam("token") String token,
                                      @RequestBody ForgetPwdDTO forgetPwdDTO) {
        Integer id = userService
                .resetPwd(email, forgetPwdDTO.getPassword(), forgetPwdDTO.getCheckPassword(), token);
        return Response.success(id);
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

    @SessionUser
    @PutMapping
    public Response<UserVO> updateUser(HttpSession session,
                                       @SessionUser UserVO userVO,
                                       @RequestParam(value = "nickname", required = false) String nickname,
                                       @RequestParam(value = "password", required = false) String password
    ) {
        String bcryptHashPwd = null;
        String name = null;

        if (nickname != null && !nickname.isEmpty()) {
            name = nickname;
            userVO.setName(nickname);
        }
        if (password != null && !password.isEmpty()) {
            if (password.length() < 8)
                throw new BusinessException(SC_BAD_REQUEST, "密码长度不能小于8位");
            bcryptHashPwd = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        }

        userService.lambdaUpdate()
                .eq(User::getId, userVO.getId())
                .set(StringUtils.isNotBlank(name), User::getName, name)
                .set(StringUtils.isNotBlank(bcryptHashPwd), User::getPassword, bcryptHashPwd)
                .update();

        User user = userService.getById(userVO.getId());
        userVO.setUpdatedAt(user.getUpdatedAt());

        session.setAttribute(USER_VO, userVO);
        return Response.success(userVO);
    }

    @SessionUser
    @Transactional
    @PostMapping("/upload/avatar")
    public Response<UserVO> uploadAvatar(HttpSession session,
                                         @SessionUser UserVO userVO,
                                         @RequestParam("file") MultipartFile file) {
        if (file.isEmpty())
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "文件不能为空");

        String contentType = file.getContentType();
        if (!MediaType.IMAGE_JPEG_VALUE.equals(contentType)
                && !MediaType.IMAGE_PNG_VALUE.equals(contentType)) {
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "文件类型错误");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = FileUtil.getSuffix(originalFilename);

            String sourceName = userVO.getPolicy().getAvatarFileNameRule()
                    .replace(UID_PLACEHOLDER, userVO.getId().toString())
                    .replace(UUID_PLACEHOLDER, IdUtil.simpleUUID()) + DOT + extension;

            Path filePath = Paths.get(sourceName);
            Files.createDirectories(filePath.getParent());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            userService.lambdaUpdate().eq(User::getId, userVO.getId()).set(User::getAvatar, sourceName).update();

            User user = userService.getById(userVO.getId());
            userVO.setAvatar(user.getAvatar());
            userVO.setUpdatedAt(user.getUpdatedAt());
            session.setAttribute(USER_VO, userVO);

            return Response.success(userVO, "上传成功");

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "文件上传失败");
        }
    }


    @GetMapping("/avatar")
    public void getAvatar(@RequestParam("filePath") String filePath,
                            HttpServletResponse response) throws IOException {

        Path path = Paths.get(filePath).normalize();
        String fileName = path.getFileName().toString();
        log.debug("头像：{}", path);
        FileHandlerUtils.previewFile(path, fileName, response);
    }
}

