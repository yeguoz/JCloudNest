package icu.yeguo.cloudnest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.yeguo.cloudnest.exception.BusinessException;
import icu.yeguo.cloudnest.model.entity.Folder;
import icu.yeguo.cloudnest.service.CommonService;
import icu.yeguo.cloudnest.service.IFolderService;
import icu.yeguo.cloudnest.mapper.FolderMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author yeguo
 * @createDate 2025-02-15 18:13:03
 */
@Service
public class FolderServiceImpl extends ServiceImpl<FolderMapper, Folder>
        implements IFolderService {

    @Autowired
    private FolderMapper folderMapper;
    @Autowired
    private CommonService commonService;

    @Override
    public Long createFolder(Integer userId, String path, String name) {
        long parentFolderId = commonService.findFolderId(userId, path);
        if (parentFolderId == 0)
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "父文件夹不存在");
        // 不能有同名文件夹
        Long count = folderMapper.selectCount(new LambdaQueryWrapper<Folder>()
                .eq(Folder::getName, name)
                .eq(Folder::getParentId, parentFolderId)
        );
        if (count > 0)
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "存在同名文件夹");
        // 插入文件夹数据
        Folder folder = new Folder();
        folder.setUserId(userId);
        folder.setName(name);
        folder.setParentId(parentFolderId);
        int insert = folderMapper.insert(folder);
        if (insert < 1)
            throw new BusinessException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "创建目录失败");
        return folder.getId();
    }

}




