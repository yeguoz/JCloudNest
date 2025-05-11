package icu.yeguo.cloudnest.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import icu.yeguo.cloudnest.annotation.SessionUser;
import icu.yeguo.cloudnest.common.Response;
import icu.yeguo.cloudnest.exception.BusinessException;
import icu.yeguo.cloudnest.model.dto.FolderDTO;
import icu.yeguo.cloudnest.model.dto.UploadRequest;
import icu.yeguo.cloudnest.model.entity.Folder;
import icu.yeguo.cloudnest.model.vo.SubFolderVO;
import icu.yeguo.cloudnest.model.vo.UserVO;
import icu.yeguo.cloudnest.service.IFolderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Tag(name = "目录Controller")
@RestController
@RequestMapping("/folder")
public class FolderController {

    @Resource
    private IFolderService folderService;

    @SessionUser
    @PostMapping
    public Response<Long> createFolder(@SessionUser UserVO userVO,
                                       @RequestParam("path") String path,
                                       @RequestParam("name") String name) throws IOException {
        Long id = folderService.createFolder(userVO, path, name);
        return Response.success(id);
    }

    @PostMapping("/rename")
    public Response<Integer> renameFolder(@RequestParam("id") Long id,
                                          @RequestParam("name") String name) {
        int result = folderService.renameFolder(id, name);
        return Response.success(result);
    }

    @SessionUser
    @GetMapping("/sub")
    public Response<SubFolderVO> getSubFolders(@SessionUser UserVO userVO,
                                               @RequestParam(value = "path", required = false) String path,
                                               @RequestParam(value = "id", required = false) Long id) {
        long currentId = 0;
        if (path != null)
            currentId = folderService.findFolderId(userVO.getId(), path);
        if (id != null)
            currentId = id;

        BaseMapper<Folder> baseMapper = folderService.getBaseMapper();
        Folder current = folderService.getById(currentId);
        List<Folder> folders = baseMapper.selectList(new LambdaQueryWrapper<Folder>().eq(Folder::getParentId, currentId));
        List<FolderDTO> list = folders.stream().map(f -> BeanUtil.toBean(f, FolderDTO.class)).toList();
        SubFolderVO subFolderVO = new SubFolderVO(currentId, current.getParentId(), list);
        return Response.success(subFolderVO);
    }

    @PutMapping("/move")
    public Response<?> moveFolder(@RequestParam("originFolderId") Long originFolderId,
                                  @RequestParam("targetFolderId") Long targetFolderId) {
        Folder folder = folderService.getById(originFolderId);
        if (Objects.equals(folder.getParentId(), targetFolderId))
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "已在当前文件夹下");

        Folder one = folderService.getOne(new LambdaQueryWrapper<Folder>()
                .eq(Folder::getParentId, targetFolderId)
                .eq(Folder::getName, folder.getName()));
        if (one != null)
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "已存在同名文件夹");

        boolean b = folderService.lambdaUpdate().eq(Folder::getId, originFolderId).set(Folder::getParentId, targetFolderId).update();
        if (!b)
            throw new BusinessException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "文件夹移动失败");
        return Response.success(null, "文件夹移动成功");
    }

    @SessionUser
    @PostMapping("/copy")
    public Response<?> copyFolder(@SessionUser UserVO userVO,
                                  @RequestParam("originFolderId") Long originFolderId,
                                  @RequestParam("targetFolderId") Long targetFolderId,
                                  @RequestParam("folderName") String folderName,
                                  @RequestBody UploadRequest uploadRequest) {
        folderService.copyFolderRecursively(userVO, originFolderId, targetFolderId, folderName, uploadRequest);
        return Response.success(null, "文件夹复制成功");
    }

    @SessionUser
    @DeleteMapping
    public Response<?> deleteFolder(@SessionUser UserVO userVO, @RequestParam("originFolderId") Long originFolderId) {
        folderService.deleteFolderRecursively(userVO, originFolderId);
        return Response.success(null, "文件夹删除成功");
    }
}
