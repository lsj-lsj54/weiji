package com.weiji.modules.user.dto; // 请求 DTO 包

import jakarta.validation.constraints.NotBlank; // Bean Validation
import lombok.Data; // Lombok 样板代码生成

@Data // Lombok：getter/setter/equals/hashCode
public class LoginRequest { // 定义类 LoginRequest

    @NotBlank(message = "手机号不能为空") // 校验：不能空白
    private String phone; // 字段 手机号

    @NotBlank(message = "密码不能为空") // 校验：不能空白
    private String password; // 字段 password

    private String smsCode; // 字段 smsCode
}
