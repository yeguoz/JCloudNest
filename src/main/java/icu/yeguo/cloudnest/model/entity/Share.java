package icu.yeguo.cloudnest.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName cn_shares
 */
@TableName(value ="cn_shares")
@Data
public class Share implements Serializable {

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = -1964631921540782018L;
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @TableField(value = "user_id")
    private Integer userId;
    @TableField(value = "source_id")
    private Long sourceId;
    @TableField(value = "user_file_id")
    private Long userFileId;
    @TableField(value = "short_id")
    private String shortId;
    @TableField(value = "source_name")
    private String sourceName;
    @TableField(value = "password_enabled")
    private Integer passwordEnabled;
    @TableField(value = "password")
    private String password;
    @TableField(value = "is_dir")
    private Integer isDir;
    @TableField(value = "visit_count")
    private Integer visitCount;
    @TableField(value = "remaining_downloads")
    private Integer remainingDownloads;
    @TableField(value = "preview_enabled")
    private Integer previewEnabled;
    @TableField(value = "expire_time_enabled")
    private Integer expireTimeEnabled;
    @TableField(value = "expire_time")
    private LocalDateTime expireTime;
    @TableField(value = "created_at")
    private LocalDateTime createdAt;
    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;
    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;
}