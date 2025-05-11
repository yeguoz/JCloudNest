package icu.yeguo.cloudnest.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FolderDTO {
    private Long id;
    private Integer userId;
    private String name;
    private Long parentId;
    private LocalDateTime createdAt;
}