package icu.yeguo.cloudnest.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(name = "FileFolderItem", description = "文件传输对象", requiredProperties = {
        "id",
        "userId",
        "name",
        "parentId",
        "size",
        "fileHash",
        "sourceName",
        "folderId",
        "fileId",
        "userFileId",
        "type",
        "updatedAt"
})
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FileFolderItem {
    @Schema(description = "id")
    private String id;
    @Schema(description = "用户id")
    private Integer userId;
    @Schema(description = "文件或文件夹名字")
    private String name;
    @Schema(description = "父文件夹id")
    private Long parentId;
    @Schema(description = "文件大小")
    private Long size;
    @Schema(description = "文件hash值")
    private String fileHash;
    @Schema(description = "文件前256KBhash值")
    private String headerHash;
    @Schema(description = "文件源名字")
    private String sourceName;
    @Schema(description = "文件夹id")
    private Long folderId;
    @Schema(description = "文件id")
    private Long fileId;
    @Schema(description = "用户文件id")
    private Long userFileId;
    @Schema(description = "类型", example = "file or folder")
    private String type;
    @Schema(description = "用户文件或文件夹更新时间(重命名时候)")
    private LocalDateTime updatedAt;
}
