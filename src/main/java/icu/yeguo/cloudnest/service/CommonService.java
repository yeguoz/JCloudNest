package icu.yeguo.cloudnest.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import icu.yeguo.cloudnest.constant.CommonConstant;
import icu.yeguo.cloudnest.mapper.FolderMapper;
import icu.yeguo.cloudnest.model.entity.Folder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommonService {

    @Autowired
    private FolderMapper folderMapper;

    public long findFolderId(int userId, String path) {
        String[] paths;
        if (CommonConstant.ROOT.equals(path)) {
            paths = new String[1];
        } else {
            paths = path.split(CommonConstant.ROOT);
        }
        paths[0] = CommonConstant.ROOT;

        long folderId = 0;
        for (String item : paths) {
            if (CommonConstant.ROOT.equals(item)) {
                Folder folder = folderMapper.selectOne(new LambdaQueryWrapper<Folder>()
                        .eq(Folder::getUserId, userId)
                        .eq(Folder::getName, item)
                        .isNull(Folder::getParentId));
                folderId = folder != null ? folder.getId() : 0;
            } else {
                if (folderId == 0)
                    break;
                Folder folder = folderMapper.selectOne(new LambdaQueryWrapper<Folder>()
                        .eq(Folder::getUserId, userId)
                        .eq(Folder::getName, item)
                        .eq(Folder::getParentId, folderId));
                folderId = folder != null ? folder.getId() : 0;
            }
        }
        return folderId;
    }
}
