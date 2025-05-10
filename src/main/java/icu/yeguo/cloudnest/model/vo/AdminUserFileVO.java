package icu.yeguo.cloudnest.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserFileVO {
    private Long id;
    private Integer userId;
    private Long fileId;
    private String username;
    private String filename;
    private LocalDateTime createdAt;
}