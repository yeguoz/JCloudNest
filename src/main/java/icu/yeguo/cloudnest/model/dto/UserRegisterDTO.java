package icu.yeguo.cloudnest.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(name = "UserRegisterDTO",description = "用户注册数据")
@Data
public class UserRegisterDTO {
    private String email;
    private String password;
    private String checkPassword;
}
