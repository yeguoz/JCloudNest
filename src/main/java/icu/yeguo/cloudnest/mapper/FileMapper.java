package icu.yeguo.cloudnest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import icu.yeguo.cloudnest.model.entity.File;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
* @author yeguo
* @createDate 2025-02-15 18:13:03
*/
public interface FileMapper extends BaseMapper<File> {

    @Delete("DELETE FROM cn_files WHERE id = #{id}")
    Boolean deleteByIdPhysically(@Param("id") Long id);
}




