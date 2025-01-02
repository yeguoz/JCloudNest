package icu.yeguo.cloudnest.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.yeguo.cloudnest.mapper.GroupMapper;
import icu.yeguo.cloudnest.model.entity.Group;
import icu.yeguo.cloudnest.service.IGroupService;
import org.springframework.stereotype.Service;

/**
 *
 * @author yeguo
 * @since 2024-12-31
 */
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements IGroupService {

}
