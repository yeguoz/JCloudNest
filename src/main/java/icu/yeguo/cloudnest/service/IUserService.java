package icu.yeguo.cloudnest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.yeguo.cloudnest.model.dto.UserLoginDTO;
import icu.yeguo.cloudnest.model.dto.UserRegisterDTO;
import icu.yeguo.cloudnest.model.entity.User;
import icu.yeguo.cloudnest.model.vo.AdminUserVO;
import icu.yeguo.cloudnest.model.vo.UserVO;
import jakarta.servlet.http.HttpSession;

import java.util.List;

/**
 * @author yeguo
 * @since 2024-12-31
 */
public interface IUserService extends IService<User> {
    UserVO createUser(HttpSession session,UserRegisterDTO userRegisterDTO, String captcha);

    UserVO login(HttpSession session, UserLoginDTO userLoginDTO, String captcha);

    List<AdminUserVO> getUsersByAdmin();

    void forgetPwd(String email);

    Integer resetPwd(String email, String password, String checkPassword, String token);
}
