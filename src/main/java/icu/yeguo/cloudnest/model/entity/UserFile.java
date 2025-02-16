package icu.yeguo.cloudnest.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName cn_user_files
 */
@TableName(value ="cn_user_files")
@Data
public class UserFile implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1171223106052668358L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField(value = "user_id")
    private Integer userId;
    @TableField(value = "file_id")
    private Long fileId;
    @TableField(value = "file_name")
    private String fileName;
    @TableField(value = "folder_id")
    private Long folderId;
    @TableField(value = "created_at")
    private LocalDateTime createdAt;
    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;
    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;
}