package icu.yeguo.cloudnest.mapper;

import icu.yeguo.cloudnest.model.entity.Share;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import icu.yeguo.cloudnest.model.vo.AdminShareFileVO;

import java.util.List;

/**
* @author Lenovo
* @description 针对表【cn_shares】的数据库操作Mapper
* @createDate 2025-03-20 10:15:57
* @Entity icu.yeguo.cloudnest.model.entity.Share
*/
public interface ShareMapper extends BaseMapper<Share> {

    List<AdminShareFileVO> getSharedFilesByAdmin();
}




