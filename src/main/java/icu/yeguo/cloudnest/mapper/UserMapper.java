package icu.yeguo.cloudnest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import icu.yeguo.cloudnest.model.entity.User;
import icu.yeguo.cloudnest.model.vo.AdminUserVO;

import java.util.List;

/**
 *
 * @author yeguo
 * @since 2024-12-31
 */
public interface UserMapper extends BaseMapper<User> {

    List<AdminUserVO> getUsersByAdmin();
}
