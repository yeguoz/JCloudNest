package icu.yeguo.cloudnest.model.vo;

import icu.yeguo.cloudnest.model.dto.FileFolderItem;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShareVO {
    private UserVO userVO;
    private Long sourceId;
    private String sourceName;
    private Long size;
    private String filename;
    private Boolean isDir;
    private Boolean previewEnabled;
    private Integer visitCount;
    private Integer remainingDownloads;
    private List<FileFolderItem> list;
    private LocalDateTime createdAt;
}