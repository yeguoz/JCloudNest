package icu.yeguo.cloudnest.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminShareFileVO {
    private Integer id;
    private Integer userId;
    private String username;
    private Long sourceId;
    private Long userFileId;
    private String shortId;
    private Byte passwordEnabled;
    private String password;
    private Byte isDir;
    private Integer visitCount;
    private Integer remainingDownloads;
    private Byte previewEnabled;
    private Byte expireTimeEnabled;
    private LocalDateTime expireTime;
    private LocalDateTime createdAt;
}
