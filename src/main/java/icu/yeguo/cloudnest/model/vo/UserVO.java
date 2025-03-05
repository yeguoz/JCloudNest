package icu.yeguo.cloudnest.model.vo;

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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
