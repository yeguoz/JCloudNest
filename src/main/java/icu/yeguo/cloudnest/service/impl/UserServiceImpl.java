package icu.yeguo.cloudnest.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.yeguo.cloudnest.mapper.UserMapper;
import icu.yeguo.cloudnest.model.entity.User;
import icu.yeguo.cloudnest.service.IUserService;
import org.springframework.stereotype.Service;

/**
 *
 * @author yeguo
 * @since 2024-12-31
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
