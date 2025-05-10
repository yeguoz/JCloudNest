package icu.yeguo.cloudnest.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserVO {
    private Integer id;
    private String name;
    private String email;
    private Integer groupId;
    private String groupName;
    private Integer policyId;
    private Byte status;
    private Long usedStorage;
    private LocalDateTime createdAt;
}