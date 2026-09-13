package com.weiji.modules.user.vo; // 响应 VO 包

import lombok.AllArgsConstructor; // Lombok 样板代码生成
import lombok.Builder; // Lombok 样板代码生成
import lombok.Data; // Lombok 样板代码生成
import lombok.NoArgsConstructor; // Lombok 样板代码生成

@Data // Lombok：getter/setter/equals/hashCode
@Builder // Lombok 建造者
@NoArgsConstructor // 无参构造，Jackson 反序列化需要
@AllArgsConstructor // 全参构造
public class UserVO { // 定义类 UserVO

    private Long id; // 字段 id
    private String phone; // 字段 手机号
    private String nickname; // 字段 昵称
    private String avatar; // 字段 头像 URL
    private Integer status; // 字段 状态
}
