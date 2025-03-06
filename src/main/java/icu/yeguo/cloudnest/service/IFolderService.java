package icu.yeguo.cloudnest.service;

import icu.yeguo.cloudnest.model.entity.Folder;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;

/**
 * @author yeguo
 * @createDate 2025-02-15 18:13:03
 */
public interface IFolderService extends IService<Folder> {
    Long createFolder(Integer userId, String path, String name) throws IOException;
}
