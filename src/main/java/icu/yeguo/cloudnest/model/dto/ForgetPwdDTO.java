package icu.yeguo.cloudnest.model.dto;

import lombok.Data;

@Data
public class ForgetPwdDTO {
    private String password;
    private String checkPassword;
}