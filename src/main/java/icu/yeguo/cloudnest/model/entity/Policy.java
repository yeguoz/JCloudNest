package icu.yeguo.cloudnest.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
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
    @TableField(value = "public_dir_name_rule")
    private String publicDirNameRule;
    @TableField(value = "public_file_name_rule")
    private String publicFileNameRule;
    @TableField(value = "private_dir_name_rule")
    private String privateDirNameRule;
    @TableField(value = "private_file_name_rule")
    private String privateFileNameRule;
    @TableField(value = "created_at")
    private LocalDateTime createdAt;
    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;
    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;
}