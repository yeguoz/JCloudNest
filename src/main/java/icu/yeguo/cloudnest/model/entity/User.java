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
@TableName("cn_user")
public class User implements Serializable {
    @Serial
    @TableField(exist=false)
    private static final long serialVersionUID = 2902694116255781435L;
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @TableField(value = "group_id")
    private Integer groupId;
    @TableField(value = "name")
    private String name;
    @TableField(value = "password")
    private String password;
    @TableField(value = "email")
    private String email;
    @TableField(value = "status")
    private Byte status;
    @TableField(value = "avatar")
    private String avatar;
    @TableField(value = "used_storage")
    private Long usedStorage;
    @TableField(value = "created_at")
    private LocalDateTime createdAt;
    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;
    @TableLogic(value = "0", delval = "1")
    private Byte isDeleted;

}
