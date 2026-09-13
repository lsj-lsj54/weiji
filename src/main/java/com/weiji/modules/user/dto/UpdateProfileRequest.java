package com.weiji.modules.user.dto; // 请求 DTO 包

import jakarta.validation.constraints.Size; // Bean Validation
import lombok.Data; // Lombok 样板代码生成

@Data // Lombok：getter/setter/equals/hashCode
public class UpdateProfileRequest { // 定义类 UpdateProfileRequest

    @Size(max = 64, message = "昵称过长") // 校验：长度限制
    private String nickname; // 字段 昵称

    @Size(max = 512, message = "头像地址过长") // 校验：长度限制
    private String avatar; // 字段 头像 URL
}
