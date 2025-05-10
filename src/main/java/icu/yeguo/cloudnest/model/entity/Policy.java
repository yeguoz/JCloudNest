package icu.yeguo.cloudnest.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 
 * @TableName cn_policies
 */
@TableName(value ="cn_policies")
@Data
public class Policy implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = -1089800767315842813L;
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @TableField(value = "name")
    private String name;
    @TableField(value = "type")
    private String type;
    @TableField(value = "file_dir_name_rule")
    private String fileDirNameRule;
    @TableField(value = "avatar_file_name_rule")
    private String avatarFileNameRule;
    @TableField(value = "file_name_rule")
    private String fileNameRule;
    @TableField(value = "chunk_dir_name_rule")
    private String chunkDirNameRule;
    @TableField(value = "chunk_file_name_rule")
    private String chunkFileNameRule;
    @TableField(value = "empty_file_name_rule")
    private String emptyFileNameRule;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "created_at")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;
    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;
}