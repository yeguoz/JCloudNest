package icu.yeguo.cloudnest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.yeguo.cloudnest.model.entity.File;

import java.io.IOException;

/**
 * @author yeguo
 * @createDate 2025-02-15 18:13:03
 */
public interface IFileService extends IService<File> {
    File createFile(int userId,String path, String name) throws IOException;
}
