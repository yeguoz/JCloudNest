package icu.yeguo.cloudnest.service.impl;


import at.favre.lib.crypto.bcrypt.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.yeguo.cloudnest.constant.HttpStatusConstant;
import icu.yeguo.cloudnest.exception.BusinessException;
import icu.yeguo.cloudnest.mapper.UserMapper;
import icu.yeguo.cloudnest.model.dto.UserRegisterDTO;
import icu.yeguo.cloudnest.model.entity.User;
import icu.yeguo.cloudnest.model.vo.UserVO;
import icu.yeguo.cloudnest.service.ISettingService;
import icu.yeguo.cloudnest.service.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static icu.yeguo.cloudnest.constant.SettingConstant.REGISTER_GROUP;
import static icu.yeguo.cloudnest.constant.UserConstant.NORMAL;

/**
 * @author yeguo
 * @since 2024-12-31
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ISettingService settingService;

    @Override
    public UserVO createUser(UserRegisterDTO userRegisterDTO) {
        String emailRegex = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        // 判断用户信息是否为空
        if (userRegisterDTO == null
                || userRegisterDTO.getEmail().isBlank()
                || userRegisterDTO.getPassword().isBlank()
                || userRegisterDTO.getCheckPassword().isBlank()
        ) {
            throw new BusinessException(HttpStatusConstant.BadRequest, "用户信息不能为空");
        }
        // 邮箱正则匹配
        if (!userRegisterDTO.getEmail().matches(emailRegex))
            throw new BusinessException(HttpStatusConstant.BadRequest, "邮箱格式不正确");
        // 密码需要大于8位
        if (userRegisterDTO.getPassword().length() < 8) {
            throw new BusinessException(HttpStatusConstant.BadRequest, "密码长度不能小于8位");
        }
        // 判断两次密码是否一致
        if (!userRegisterDTO.getPassword().equals(userRegisterDTO.getCheckPassword())) {
            throw new BusinessException(HttpStatusConstant.BadRequest, "两次密码不一致");
        }
        // 判断邮箱是否已被注册
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, userRegisterDTO.getEmail()));
        if (count > 0)
            throw new BusinessException(HttpStatusConstant.BadRequest, "该邮箱已被注册");
        // 创建用户
        User user = new User();
        // 用户组
        String groupId = settingService.getSettingValue(REGISTER_GROUP);
        user.setGroupId(Integer.parseInt(groupId));
        // 设置用户名
        String username = userRegisterDTO.getEmail().split("@")[0];
        user.setName(username);
        // bcrypt加密
        String password = userRegisterDTO.getPassword();
        String bcryptHashString = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        user.setPassword(bcryptHashString);
        // 设置邮箱
        user.setEmail(userRegisterDTO.getEmail());
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
}
