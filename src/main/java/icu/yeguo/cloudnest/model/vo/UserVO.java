package icu.yeguo.cloudnest.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import icu.yeguo.cloudnest.model.entity.Group;
import icu.yeguo.cloudnest.model.entity.Policy;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 2745103788141978920L;
    private Integer id;
    private Integer groupId;
    private String name;
    private String email;
    private Byte status;
    private String avatar;
    private Long usedStorage;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    private Group group;
    private Policy policy;
}
