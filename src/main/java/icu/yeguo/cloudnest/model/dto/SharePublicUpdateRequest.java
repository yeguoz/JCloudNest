package icu.yeguo.cloudnest.model.dto;

import lombok.Data;

@Data
public class SharePublicUpdateRequest {
    private Integer passwordEnabled;
    private String password;
}