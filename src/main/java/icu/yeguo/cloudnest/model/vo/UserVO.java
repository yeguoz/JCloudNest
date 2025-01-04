package icu.yeguo.cloudnest.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "用户信息")
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
