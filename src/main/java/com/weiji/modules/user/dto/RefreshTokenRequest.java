package com.weiji.modules.user.dto; // 请求 DTO 包

import jakarta.validation.constraints.NotBlank; // Bean Validation
import lombok.Data; // Lombok 样板代码生成

@Data // Lombok：getter/setter/equals/hashCode
public class RefreshTokenRequest { // 定义类 RefreshTokenRequest

    @NotBlank(message = "refreshToken 不能为空") // 校验：不能空白
    private String refreshToken; // 字段 refreshToken
}
