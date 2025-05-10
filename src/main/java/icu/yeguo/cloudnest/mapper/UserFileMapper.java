package icu.yeguo.cloudnest.mapper;

import icu.yeguo.cloudnest.model.entity.UserFile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import icu.yeguo.cloudnest.model.vo.AdminUserFileVO;

import java.util.List;

/**
 * @author yeguo
 * @createDate 2025-02-15 18:13:03
 */
public interface UserFileMapper extends BaseMapper<UserFile> {

    long deleteUserFile(int userId, long userFileId, long fileId);

    List<AdminUserFileVO> getUserFilesByAdmin();

    Boolean removePhysicallyById(Long userFileId);
}




