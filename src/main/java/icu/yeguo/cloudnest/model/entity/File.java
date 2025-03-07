package icu.yeguo.cloudnest.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@TableName(value ="cn_files")
@Data
public class File implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 5658897130570404449L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField(value = "size")
    private Long size;
    @TableField(value = "file_hash")
    private String fileHash;
    @TableField(value = "source_name")
    private String sourceName;
    @TableField(value = "is_public")
    private Integer isPublic;
    @TableField(value = "reference_count")
    private Integer referenceCount;
    @TableField(value = "created_at")
    private LocalDateTime createdAt;
    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;
    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;

}