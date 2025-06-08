package icu.yeguo.cloudnest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.yeguo.cloudnest.model.entity.File;
import icu.yeguo.cloudnest.model.vo.UserVO;

import java.io.IOException;

/**
 * @author yeguo
 * @createDate 2025-02-15 18:13:03
 */
public interface IFileService extends IService<File> {
    File createFile(UserVO userVO, String path, String filename) throws IOException;

    Boolean increaseReferenceCount(Long fileId);

    Boolean decreaseReferenceCount(Long fileId);

    Boolean deleteByIdPhysically(Long id);
}
