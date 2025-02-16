package icu.yeguo.cloudnest.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName cn_folders
 */
@TableName(value ="cn_folders")
@Data
public class Folder implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = -1338624954064527012L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField(value = "user_id")
    private Integer userId;
    @TableField(value = "name")
    private String name;
    @TableField(value = "parent_id")
    private Long parentId;
    @TableField(value = "created_at")
    private LocalDateTime createdAt;
    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;
    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;

}