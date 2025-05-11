package icu.yeguo.cloudnest.controller;

import cn.hutool.core.bean.BeanUtil;
import icu.yeguo.cloudnest.annotation.SessionUser;
import icu.yeguo.cloudnest.model.dto.FileFolderItem;

import icu.yeguo.cloudnest.model.dto.SharePreviewUpdateRequest;
import icu.yeguo.cloudnest.model.dto.SharePublicUpdateRequest;
import icu.yeguo.cloudnest.model.entity.*;
import icu.yeguo.cloudnest.model.vo.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import icu.yeguo.cloudnest.common.Response;
import icu.yeguo.cloudnest.exception.BusinessException;
import icu.yeguo.cloudnest.model.dto.ShortLinkDTO;
import icu.yeguo.cloudnest.service.*;
import icu.yeguo.cloudnest.util.Base62EncoderUtils;
import icu.yeguo.cloudnest.util.FileHandlerUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "分享Controller")
@RestController
@RequestMapping("/s")
public class ShareController {

    @Resource
    private IShareService shareService;
    @Resource
    private IFolderService folderService;
    @Resource
    private IFileService fileService;
    @Resource
    private IUserService userService;
    @Resource
    private IUserFileService userFileService;

    @Value("${app.time-zone}")
    private String zone;

    @SessionUser
    @Transactional
    @PostMapping
    public Response<String> createShortLink(@SessionUser UserVO userVO,
                                            @RequestBody ShortLinkDTO shortLinkDTO) {
        Byte shareEnabled = userVO.getGroup().getShareEnabled();

        if (shareEnabled < 1) {
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "分享功能未启用");
        }
        if (shortLinkDTO.getPasswordEnabled() && (shortLinkDTO.getPassword() == null || shortLinkDTO.getPassword().isEmpty()))
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "密码不能为空");
        if (shortLinkDTO.getExpireTimeEnabled() && shortLinkDTO.getExpireTime() == null)
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "过期时间不能为空");

        Share share = new Share();
        // 将UTC时间转换为指定时区的时间
        String utcExpireTime = shortLinkDTO.getExpireTime();
        LocalDateTime expireTime = null;
        if (utcExpireTime != null) {
            Instant instant = Instant.parse(utcExpireTime);
            ZoneId zoneId = ZoneId.of(zone);
            expireTime = instant.atZone(zoneId).toLocalDateTime();
        }

        if (shortLinkDTO.getIsDir() > 0) {
            Folder folder = folderService.getById(shortLinkDTO.getSourceId());
            share.setSourceName(folder.getName());
        } else {
            File file = fileService.getById(shortLinkDTO.getSourceId());
            share.setSourceName(file.getSourceName());
        }

        share.setUserId(shortLinkDTO.getUserId());
        share.setSourceId(shortLinkDTO.getSourceId());
        share.setUserFileId(shortLinkDTO.getUserFileId());
        share.setPasswordEnabled(shortLinkDTO.getPasswordEnabled() ? 1 : 0);
        share.setPassword(shortLinkDTO.getPassword());
        share.setIsDir(shortLinkDTO.getIsDir());
        share.setVisitCount(0);
        share.setRemainingDownloads(shortLinkDTO.getRemainingDownloads());
        share.setPreviewEnabled(shortLinkDTO.getPreviewEnabled() ? 1 : 0);
        share.setExpireTimeEnabled(shortLinkDTO.getExpireTimeEnabled() ? 1 : 0);
        share.setExpireTime(expireTime);
        boolean save = shareService.save(share);
        if (!save)
            throw new BusinessException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "创建分享链接失败");

        String shortId = Base62EncoderUtils.generateShortId(share.getId());
        share.setShortId(shortId);
        boolean b = shareService.updateById(share);
        if (!b)
            throw new BusinessException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "创建分享链接失败");
        return Response.success(shortId);
    }

    @GetMapping("/info/{shortId}")
    public Response<ShareInfoVO> getSharedFileInfo(@PathVariable String shortId) {
        Share share = shareService.getOne(new LambdaQueryWrapper<Share>().eq(Share::getShortId, shortId));
        if (share == null)
            throw new BusinessException(HttpServletResponse.SC_NOT_FOUND, "分享链接不存在");

        LocalDateTime now = LocalDateTime.now();
        if (share.getExpireTimeEnabled() > 0 && now.isAfter(share.getExpireTime())) {
            shareService.lambdaUpdate()
                    .eq(Share::getId, share.getId())
                    .isNull(Share::getDeletedAt)
                    .set(Share::getDeletedAt, LocalDateTime.now())
                    .update();
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "分享链接已过期");
        }

        ShareInfoVO shareInfoVO = new ShareInfoVO();

        User user = userService.getById(share.getUserId());
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        shareInfoVO.setUserVO(userVO);

        shareInfoVO.setPasswordEnabled(share.getPassword() != null);
        shareInfoVO.setIsDir(share.getIsDir() > 0);
        shareInfoVO.setSourceName(share.getSourceName());
        shareInfoVO.setCreatedAt(share.getCreatedAt());

        return Response.success(shareInfoVO);
    }

    @GetMapping("/{shortId}")
    public Response<ShareVO> getSharedFile(@PathVariable String shortId,
                                           @RequestParam(value = "password", required = false) String password,
                                           @RequestParam(value = "path", required = false) String path) {
        Share share = shareService.getOne(new LambdaQueryWrapper<Share>().eq(Share::getShortId, shortId));
        if (share == null)
            throw new BusinessException(HttpServletResponse.SC_NOT_FOUND, "分享链接不存在");
        if (password == null && share.getPasswordEnabled() != 0)
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "请输入密码");
        if (password != null && share.getPasswordEnabled() != 0 && !share.getPassword().equals(password))
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "密码错误");

        ShareVO shareVO = new ShareVO();

        User user = userService.getById(share.getUserId());
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        shareVO.setUserVO(userVO);

        shareVO.setSourceId(share.getSourceId());
        shareVO.setSourceName(share.getSourceName());
        shareVO.setIsDir(share.getIsDir() > 0);
        shareVO.setPreviewEnabled(share.getPreviewEnabled() > 0);
        shareVO.setVisitCount(share.getVisitCount() == null ? 0 : share.getVisitCount());
        shareVO.setRemainingDownloads(share.getRemainingDownloads());
        shareVO.setCreatedAt(share.getCreatedAt());

        if (share.getIsDir() > 0) {
            List<FileFolderItem> list;
            if (path == null || path.isEmpty()) {
                list = userFileService.getUserFilesByFolderId(share.getSourceId());
            } else {
                Long parentId = folderService.queryFolderIdByRelativePath(share.getSourceId(), path);
                list = userFileService.getUserFilesByFolderId(parentId);
            }
            shareVO.setList(list);
        } else {
            File file = fileService.getOne(new LambdaQueryWrapper<File>().eq(File::getId, share.getSourceId()));
            UserFile userFile = userFileService.getById(share.getUserFileId());
            shareVO.setFilename(userFile.getFileName());
            shareVO.setSize(file.getSize());
        }

        shareService.lambdaUpdate().set(Share::getId, share.getId()).setSql("visit_count = visit_count + 1");
        return Response.success(shareVO);
    }

    @SessionUser
    @GetMapping("/list")
    public Response<List<MyShareVO>> getSharedFileList(@SessionUser UserVO userVO) {
        List<Share> shares = shareService.getBaseMapper()
                .selectList(new LambdaQueryWrapper<Share>().eq(Share::getUserId, userVO.getId()));
        List<MyShareVO> list = shares.stream().map(share -> {
            MyShareVO bean = BeanUtil.toBean(share, MyShareVO.class);
            UserFile userFile;
            if (bean.getIsDir() == 0) {
                userFile = userFileService.getById(bean.getUserFileId());
                bean.setFileName(userFile.getFileName());
            }
            return bean;
        }).toList();
        return Response.success(list);
    }

    @PutMapping("/public/{id}")
    public Response<Integer> updatePwdEnabled(@PathVariable("id") Integer id,
                                              @RequestBody SharePublicUpdateRequest sharePublicUpdateRequest) {
        System.out.println(sharePublicUpdateRequest.getPassword());
        System.out.println(sharePublicUpdateRequest.getPasswordEnabled());
        shareService.lambdaUpdate()
                .eq(Share::getId, id)
                .set(Share::getPasswordEnabled, sharePublicUpdateRequest.getPasswordEnabled())
                .set(Share::getPassword, sharePublicUpdateRequest.getPassword())
                .update();
        return Response.success(id);
    }

    @PutMapping("/preview/{id}")
    public Response<Integer> updatePreEnabled(@PathVariable("id") Integer id,
                                              @RequestBody SharePreviewUpdateRequest sharePreviewUpdateRequest) {
        shareService.lambdaUpdate()
                .eq(Share::getId, id)
                .set(Share::getPreviewEnabled, sharePreviewUpdateRequest.getPreviewEnabled())
                .update();
        return Response.success(id);
    }

    @DeleteMapping("/{id}")
    public Response<Integer> deleteShare(@PathVariable("id") Integer id) {
        shareService.removeById(id);
        return Response.success(id);
    }

    @GetMapping("/preview")
    public void previewFile(@RequestParam("filePath") String filePath,
                            @RequestParam(value = "shortId") String shortId,
                            HttpServletResponse response) throws IOException {
        Share share = preHandler(shortId);
        if (share.getPreviewEnabled() != 1)
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "不允许预览");
        shareService.lambdaUpdate()
                .eq(Share::getId, share.getId())
                .setSql("visit_count = visit_count + 1")
                .update();

        Path path = Paths.get(filePath).normalize();
        String fileName = path.getFileName().toString();
        log.debug("预览分享文件：{}", path);
        FileHandlerUtils.previewFile(path, fileName, response);
    }

    @GetMapping("/download")
    public void downloadFile(@RequestParam("filePath") String filePath,
                             @RequestParam(value = "shortId") String shortId,
                             HttpServletResponse response) throws IOException {
        Share share = preHandler(shortId);
        if (share.getRemainingDownloads() <= 0) {
            shareService.lambdaUpdate().eq(Share::getShortId, shortId)
                    .set(Share::getDeletedAt, LocalDateTime.now())
                    .update();
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "分享链接已失效");
        }
        // 减少下载次数
        shareService.lambdaUpdate()
                .eq(Share::getShortId, shortId)
                .set(Share::getRemainingDownloads, share.getRemainingDownloads() - 1)
                .update();

        Path path = Paths.get(filePath).normalize();
        String fileName = path.getFileName().toString();
        log.debug("下载分享文件：{}", path);
        FileHandlerUtils.downloadFile(path, fileName, response);
    }

    private Share preHandler(@RequestParam("shortId") String shortId) {
        Share share = shareService.getOne(new LambdaQueryWrapper<Share>().eq(Share::getShortId, shortId));
        if (share == null)
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "分享链接无效");
        if (share.getExpireTime() != null && share.getExpireTime().isBefore(LocalDateTime.now()))
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "分享链接已过期");
        return share;
    }

}