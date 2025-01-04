package icu.yeguo.cloudnest.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * @TableName cn_setting
 */
@TableName(value = "cn_setting")
@Data
public class Setting implements Serializable {

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 2187515482053108385L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @TableField(value = "type")
    private String type;
    @TableField(value = "name")
    private String name;
    @TableField(value = "value")
    private String value;
    @TableField(value = "created_at")
    private Date createdAt;
    @TableField(value = "updated_at")
    private Date updatedAt;
    @TableLogic
    private Integer isDeleted;

}