package icu.yeguo.cloudnest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.yeguo.cloudnest.model.dto.UserRegisterDTO;
import icu.yeguo.cloudnest.model.entity.User;
import icu.yeguo.cloudnest.model.vo.UserVO;

/**
 *
 * @author yeguo
 * @since 2024-12-31
 */
public interface IUserService extends IService<User> {
    UserVO createUser(UserRegisterDTO userRegisterDTO);
}
