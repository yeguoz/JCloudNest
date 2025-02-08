package icu.yeguo.cloudnest.service.impl;


import at.favre.lib.crypto.bcrypt.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.yeguo.cloudnest.exception.BusinessException;
import icu.yeguo.cloudnest.mapper.UserMapper;
import icu.yeguo.cloudnest.model.dto.UserLoginDTO;
import icu.yeguo.cloudnest.model.dto.UserRegisterDTO;
import icu.yeguo.cloudnest.model.entity.User;
import icu.yeguo.cloudnest.model.vo.UserVO;
import icu.yeguo.cloudnest.service.ISettingService;
import icu.yeguo.cloudnest.service.IUserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static icu.yeguo.cloudnest.constant.CommonConstant.*;
import static icu.yeguo.cloudnest.constant.SettingConstant.*;
import static icu.yeguo.cloudnest.constant.UserConstant.*;
import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

/**
 * @author yeguo
 * @since 2024-12-31
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ISettingService settingService;

    @Override
    public UserVO createUser(HttpSession session, UserRegisterDTO userRegisterDTO, String captcha) {
        if (userRegisterDTO == null)
            throw new BusinessException(SC_BAD_REQUEST, "用户信息不能为空");
        String email = userRegisterDTO.getEmail();
        String password = userRegisterDTO.getPassword();
        String checkPassword = userRegisterDTO.getCheckPassword();
        // 校验邮箱和密码
        if (email.isBlank() || password.isBlank() || checkPassword.isBlank())
            throw new BusinessException(SC_BAD_REQUEST, "用户信息不能为空");
        if (isInvalidEmail(email))
            throw new BusinessException(SC_BAD_REQUEST, "邮箱格式不正确");
        if (password.length() < 8 || checkPassword.length() < 8)
            throw new BusinessException(SC_BAD_REQUEST, "密码长度不能小于8位");
        if (!password.equals(checkPassword))
            throw new BusinessException(SC_BAD_REQUEST, "两次密码不一致");
        // 校验验证码
        String captchaEnabled = settingService.getSettingValue(AUTH, REGISTER_CAPTCHA);
        checkVerificationCode(session, captcha, captchaEnabled, SESSION_CAPTCHA_REGISTER);
        // 校验邮箱是否已被注册
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (count > 0)
            throw new BusinessException(SC_BAD_REQUEST, "该邮箱已被注册");
        // 创建用户
        User user = new User();
        // 用户组
        String groupId = settingService.getSettingValue(AUTH, REGISTER_GROUP);
        user.setGroupId(Integer.parseInt(groupId));
        // 设置用户名
        user.setName(email.split("@")[0]);
        // bcrypt加密
        String bcryptHashPwd = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        user.setPassword(bcryptHashPwd);
        // 设置邮箱
        user.setEmail(email);
        // （0正常 1）
        user.setStatus((byte) NORMAL);
        // avatar
        user.setAvatar("");
        // 已使用存储
        user.setUsedStorage(0L);

        userMapper.insert(user);
        User currentUser = userMapper.selectById(user.getId());
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(currentUser, userVO);
        return userVO;
    }

    @Override
    public UserVO login(HttpSession session, UserLoginDTO userLoginDTO, String captcha) {
        if (userLoginDTO == null)
            throw new BusinessException(SC_BAD_REQUEST, "用户信息不能为空");
        String email = userLoginDTO.getEmail();
        String password = userLoginDTO.getPassword();
        if (email.isBlank() || password.isBlank())
            throw new BusinessException(SC_BAD_REQUEST, "用户信息不能为空");
        if (isInvalidEmail(email))
            throw new BusinessException(SC_BAD_REQUEST, "邮箱格式不正确");
        if (password.length() < 8)
            throw new BusinessException(SC_BAD_REQUEST, "密码长度不能小于8位");
        // 校验验证码
        String captchaEnabled = settingService.getSettingValue(AUTH, LOGIN_CAPTCHA);
        checkVerificationCode(session, captcha, captchaEnabled, SESSION_CAPTCHA_LOGIN);
        // 校验用户是否存在
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null)
            throw new BusinessException(SC_BAD_REQUEST, "该邮箱未注册");
        // 校验密码
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
        if (!result.verified)
            throw new BusinessException(SC_BAD_REQUEST, "密码错误");
        // 校验用户状态
        if (user.getStatus() == (byte) BANNED)
            throw new BusinessException(SC_BAD_REQUEST, "该用户已被封禁");

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        session.setAttribute(USER_VO, userVO);
        return userVO;
    }

    private boolean isInvalidEmail(String email) {
        return !email.matches(EMAIL_REGEX);
    }

    private void checkVerificationCode(HttpSession session, String captcha, String captchaEnabled, String captchaType) {
        if (captcha == null)
            throw new BusinessException(SC_BAD_REQUEST, "验证码不能为空");
        String captchaMaxAge = SESSION_CAPTCHA_LOGIN.equals(captchaType)
                ? SESSION_CAPTCHA_LOGIN_MAX_AGE : SESSION_CAPTCHA_REGISTER_MAX_AGE;
        log.debug("captchaEnabled: {},captcha:{} ", captchaEnabled, captcha);
        if (captchaEnabled.equals(TRUE)) {
            String sessionCaptcha = (String) session.getAttribute(captchaType);
            long sessionCaptchaMaxAge = (long) session.getAttribute(captchaMaxAge);
            if (sessionCaptcha == null || sessionCaptchaMaxAge < System.currentTimeMillis())
                throw new BusinessException(SC_BAD_REQUEST, "验证码已过期");
            if (!sessionCaptcha.equalsIgnoreCase(captcha))
                throw new BusinessException(SC_BAD_REQUEST, "验证码错误");
        }
    }
}
