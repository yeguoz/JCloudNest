package icu.yeguo.cloudnest.service;

import icu.yeguo.cloudnest.model.entity.UserFile;
import com.baomidou.mybatisplus.extension.service.IService;
import icu.yeguo.cloudnest.model.vo.FileVO;

/**
* @author yeguo
* @createDate 2025-02-15 18:13:03
*/
public interface IUserFileService extends IService<UserFile> {
    FileVO getUserFiles(Integer userId, String path);
}
