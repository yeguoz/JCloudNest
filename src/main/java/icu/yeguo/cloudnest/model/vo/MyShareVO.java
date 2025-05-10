package icu.yeguo.cloudnest.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MyShareVO {
    private Integer id;
    private Long sourceId;
    private Long userFileId;
    private String shortId;
    private String sourceName;
    private Integer passwordEnabled;
    private String password;
    private Integer isDir;
    private Integer visitCount;
    private Integer remainingDownloads;
    private Integer previewEnabled;
    private Integer expireTimeEnabled;
    private LocalDateTime expireTime;
    private LocalDateTime createdAt;
    private String fileName;
}