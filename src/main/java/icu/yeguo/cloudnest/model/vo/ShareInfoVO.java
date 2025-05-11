package icu.yeguo.cloudnest.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShareInfoVO {
    private UserVO userVO;
    private Boolean passwordEnabled;
    private Boolean isDir;
    private String sourceName;
    private LocalDateTime createdAt;
}