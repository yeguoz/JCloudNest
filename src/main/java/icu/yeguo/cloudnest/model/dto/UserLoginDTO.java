package icu.yeguo.cloudnest.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(name = "UserLoginDTO",description = "用户登录数据",requiredProperties={"email","password"})
@Data
public class UserLoginDTO {
    @Schema(description = "邮箱", example = "123@163.com")
    private String email;
    @Schema(description = "密码")
    private String password;
}
