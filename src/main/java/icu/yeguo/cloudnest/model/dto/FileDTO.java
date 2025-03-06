package icu.yeguo.cloudnest.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FileDTO {
    private String id;
    private Integer userId;
    private String name;
    private Long parentId;
    private Long size;
    private String fileHash;
    private String sourceName;
    private Long folderId;
    private String type;
    private LocalDateTime updatedAt;
}
