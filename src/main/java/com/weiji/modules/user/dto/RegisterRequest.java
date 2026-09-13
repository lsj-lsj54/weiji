package com.weiji.modules.user.dto; // 请求 DTO 包

import jakarta.validation.constraints.NotBlank; // Bean Validation
import jakarta.validation.constraints.Size; // Bean Validation
import lombok.Data; // Lombok 样板代码生成

@Data // Lombok：getter/setter/equals/hashCode
public class RegisterRequest { // 定义类 RegisterRequest

    @NotBlank(message = "手机号不能为空") // 校验：不能空白
    private String phone; // 字段 手机号

    @NotBlank(message = "密码不能为空") // 校验：不能空白
    @Size(min = 6, max = 32, message = "密码长度需为 6-32 位") // 校验：长度限制
    private String password; // 字段 password

    private String nickname; // 字段 昵称

    /** 预留短信验证码，当前版本不校验 */
    private String smsCode; // 字段 smsCode
}
