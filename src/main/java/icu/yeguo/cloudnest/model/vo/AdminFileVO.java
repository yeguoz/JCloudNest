package icu.yeguo.cloudnest.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminFileVO {
    private Long id;
    private Long size;
    private String fileHash;
    private String headerHash;
    private String sourceName;
    private Integer referenceCount;
    private LocalDateTime createdAt;
}
