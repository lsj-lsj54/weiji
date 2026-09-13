package com.weiji.modules.user.vo; // 响应 VO 包

import lombok.Builder; // Lombok 样板代码生成
import lombok.Data; // Lombok 样板代码生成

@Data // Lombok：getter/setter/equals/hashCode
@Builder // Lombok 建造者
public class TokenVO { // 定义类 TokenVO

    private String accessToken; // 字段 accessToken
    private String refreshToken; // 字段 refreshToken
    private Long expiresIn; // 字段 expiresIn
    private String tokenType; // 字段 tokenType
}
