package icu.yeguo.cloudnest.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(name = "UserRegisterDTO",description = "用户注册数据",requiredProperties={"email","password","checkPassword"})
@Data
public class UserRegisterDTO {
    @Schema(description = "邮箱", example = "123@163.com")
    private String email;
    @Schema(description = "密码")
    private String password;
    @Schema(description = "重复密码")
    private String checkPassword;
}
