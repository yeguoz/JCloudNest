package icu.yeguo.cloudnest.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * @author yeguo
 * @since 2024-12-31
 */
@Data
@TableName("cn_groups")
public class Group implements Serializable {
    @Serial
    @TableField(exist=false)
    private static final long serialVersionUID = 1185000638293438649L;
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @TableField(value = "name")
    private String name;
    @TableField(value = "policy_id")
    private Integer policyId;
    @TableField(value = "max_storage")
    private Long maxStorage;
    @TableField(value = "share_enabled")
    private Byte shareEnabled;
    @TableField(value = "created_at")
    private LocalDateTime createdAt;
    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;
    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;
}
