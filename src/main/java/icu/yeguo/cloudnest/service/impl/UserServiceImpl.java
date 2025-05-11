package icu.yeguo.cloudnest.service.impl;


import at.favre.lib.crypto.bcrypt.BCrypt;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.yeguo.cloudnest.constant.PlaceholderConstant;
import icu.yeguo.cloudnest.exception.BusinessException;
import icu.yeguo.cloudnest.mapper.UserMapper;
import icu.yeguo.cloudnest.model.dto.UserLoginDTO;
import icu.yeguo.cloudnest.model.dto.UserRegisterDTO;
import icu.yeguo.cloudnest.model.entity.Folder;
import icu.yeguo.cloudnest.model.entity.Group;
import icu.yeguo.cloudnest.model.entity.Policy;
import icu.yeguo.cloudnest.model.entity.User;
import icu.yeguo.cloudnest.model.vo.AdminUserVO;
import icu.yeguo.cloudnest.model.vo.SettingVO;
import icu.yeguo.cloudnest.model.vo.UserVO;
import icu.yeguo.cloudnest.service.*;
import jakarta.annotation.Resource;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static icu.yeguo.cloudnest.constant.CommonConstant.*;
import static icu.yeguo.cloudnest.constant.SettingConstant.*;
import static icu.yeguo.cloudnest.constant.UserConstant.*;
import static jakarta.servlet.http.HttpServletResponse.*;

/**
 * @author yeguo
 * @since 2024-12-31
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private ISettingService settingService;
    @Resource
    private IFolderService folderService;
    @Resource
    private IGroupService groupService;
    @Resource
    private IPolicyService policyService;
    @Resource
    private Executor threadPoolExecutor;
    @Resource
    private JavaMailSender mailSender;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Transactional
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
        User one = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (one != null && one.getStatus() == NORMAL)
            throw new BusinessException(SC_BAD_REQUEST, "该邮箱已被注册");
        if (one != null && one.getStatus() == NOT_ACTIVE) {
            // 异步方法发送邮件
            sendMailAsync(email, one.getId(), "register");
            throw new BusinessException(SC_BAD_REQUEST, "该邮箱未激活，已重新发送激活邮件");
        }
        // 创建用户 设置信息
        User user = new User();
        String groupName = settingService.getSettingValue(AUTH, REGISTER_GROUP);
        Group group = groupService.getOne(new LambdaQueryWrapper<Group>().eq(Group::getName, groupName));

        String bcryptHashPwd = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        user.setGroupId(group.getId());
        user.setName(email.split("@")[0]);
        user.setPassword(bcryptHashPwd);
        user.setEmail(email);
        user.setStatus((byte) NOT_ACTIVE);
        user.setAvatar("");
        user.setUsedStorage(0L);

        userMapper.insert(user);
        User currentUser = userMapper.selectById(user.getId());
        UserVO bean = BeanUtil.toBean(currentUser, UserVO.class);

        Folder folder = new Folder();
        folder.setUserId(user.getId());
        folder.setName(ROOT);
        folderService.save(folder);

        // 异步方法发送邮件
        sendMailAsync(email, user.getId(), "register");
        return bean;
    }

    private void sendMailAsync(String to, Integer userId, String type) {
        CompletableFuture.runAsync(() -> {
            try {
                // 生成激活token存入redis 10分钟过期
                String uuidToken = IdUtil.simpleUUID();
                stringRedisTemplate.opsForValue().set(type + ":" + userId.toString(), uuidToken, 10, TimeUnit.MINUTES);

                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper;
                List<SettingVO> mail = settingService.getSettingByType("mail");
                List<SettingVO> site = settingService.getSettingByType("site");

                Map<String, String> mailMap = new HashMap<>();
                Map<String, String> siteMap = new HashMap<>();
                mail.forEach(settingVO -> mailMap.put(settingVO.getName(), settingVO.getValue()));
                site.forEach(settingVO -> siteMap.put(settingVO.getName(), settingVO.getValue()));

                String subject = type.equals("register") ? mailMap.get("registerSubject") : mailMap.get("forgetSubject");
                String username = mailMap.get("username");
                String personal = mailMap.get("personal");
                String template = type.equals("register") ? mailMap.get("registerTemplate") : mailMap.get("forgetTemplate");

                String siteUrl = siteMap.get("url")
                        .charAt(siteMap.get("url").length() - 1) == '/' ?
                        siteMap.get("url") :
                        siteMap.get("url") + "/";

                String registerLink = String.format("<a href=\"%sverify/register?id=%s&token=%s\">" +
                        String.format(siteUrl + "verify/register?id=%s&token=%s", userId, uuidToken) +
                        "</a>", siteUrl, userId, uuidToken);
                String forgetLink = String.format("<a href=\"%sreset?email=%s&token=%s\">忘记密码</a>", siteUrl, to, uuidToken);

                String text = template.replace(PlaceholderConstant.LINK_PLACEHOLDER,
                        type.equals("register") ? registerLink : forgetLink);

                helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setSubject(subject);
                helper.setFrom(username, personal);
                helper.setTo(to);
                helper.setText(text, true);

                mailSender.send(mimeMessage);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, threadPoolExecutor);
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
        if (user.getStatus() == NOT_ACTIVE)
            throw new BusinessException(SC_BAD_REQUEST, "该邮箱未激活");
        // 校验密码
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
        if (!result.verified)
            throw new BusinessException(SC_BAD_REQUEST, "用户名或密码错误");
        // 校验用户状态
        if (user.getStatus() == (byte) BANNED)
            throw new BusinessException(SC_BAD_REQUEST, "该用户已被封禁");
        // 登录成功，将用户信息存入session
        Group group = groupService.getById(user.getGroupId());
        Policy policy = policyService.findPolicyByUserId(user.getId());
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setGroup(group);
        userVO.setPolicy(policy);

        session.setAttribute(USER_VO, userVO);
        return userVO;
    }

    @Override
    public List<AdminUserVO> getUsersByAdmin() {
        return userMapper.getUsersByAdmin();
    }

    @Override
    public void forgetPwd(String email) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null)
            throw new BusinessException(SC_BAD_REQUEST, "该邮箱未注册");
        if (user.getStatus() == NOT_ACTIVE)
            throw new BusinessException(SC_BAD_REQUEST, "该邮箱未激活");
        sendMailAsync(email, user.getId(), "forget");
    }

    @Override
    public Integer resetPwd(String email, String password, String checkPassword, String token) {
        if (password == null || checkPassword == null || token == null)
            throw new BusinessException(SC_BAD_REQUEST, "参数不能为空");
        if (!password.equals(checkPassword))
            throw new BusinessException(SC_BAD_REQUEST, "两次密码不一致");
        if (password.length() < 8)
            throw new BusinessException(SC_BAD_REQUEST, "密码长度不能小于8位");
        if (isInvalidEmail(email))
            throw new BusinessException(SC_BAD_REQUEST, "邮箱格式不正确");

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null)
            throw new BusinessException(SC_BAD_REQUEST, "该邮箱未注册");
        if (user.getStatus() == NOT_ACTIVE)
            throw new BusinessException(SC_BAD_REQUEST, "该邮箱未激活");

        String t = stringRedisTemplate.opsForValue().get("forget:" + user.getId());
        if (t == null || !t.equals(token))
            throw new BusinessException(SC_BAD_REQUEST, "请求无效");
        String bcryptHashPwd = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        user.setPassword(bcryptHashPwd);
        userMapper.updateById(user);
        stringRedisTemplate.delete("forget:" + user.getId());
        return user.getId();
    }

    private boolean isInvalidEmail(String email) {
        return !email.matches(EMAIL_REGEX);
    }

    private void checkVerificationCode(HttpSession session, String captcha, String captchaEnabled, String captchaType) {
        if (!captchaEnabled.equals(TRUE))
            return;
        if (captcha == null)
            throw new BusinessException(SC_BAD_REQUEST, "验证码不能为空");
        String captchaMaxAge = SESSION_CAPTCHA_LOGIN.equals(captchaType)
                ? SESSION_CAPTCHA_LOGIN_MAX_AGE : SESSION_CAPTCHA_REGISTER_MAX_AGE;
        String sessionCaptcha = (String) session.getAttribute(captchaType);
        long sessionCaptchaMaxAge = (long) session.getAttribute(captchaMaxAge);
        if (sessionCaptcha == null || sessionCaptchaMaxAge < System.currentTimeMillis())
            throw new BusinessException(SC_BAD_REQUEST, "验证码已过期");
        if (!sessionCaptcha.equalsIgnoreCase(captcha))
            throw new BusinessException(SC_BAD_REQUEST, "验证码错误");
    }
}