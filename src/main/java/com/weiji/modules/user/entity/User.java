package com.weiji.modules.user.entity; // 数据库实体包

import com.baomidou.mybatisplus.annotation.TableLogic; // MyBatis-Plus
import com.baomidou.mybatisplus.annotation.TableName; // MyBatis-Plus
import com.weiji.common.entity.BaseEntity; // 本仓类 BaseEntity
import lombok.Data; // Lombok 样板代码生成
import lombok.EqualsAndHashCode; // Lombok 样板代码生成

@Data // Lombok：getter/setter/equals/hashCode
@EqualsAndHashCode(callSuper = true) // Lombok 相等性，含父类字段
@TableName("`user`") // 映射数据库表名
public class User extends BaseEntity { // 定义类 User，有继承

    private String phone; // 字段 手机号
    private String passwordHash; // 字段 密码哈希
    private String nickname; // 字段 昵称
    private String avatar; // 字段 头像 URL
    private Integer status; // 字段 状态
    private Integer streakDays; // 字段 连续天数
    private java.time.LocalDate lastActiveDate; // 字段 最近活跃日

    @TableLogic // 逻辑删除字段
    private Integer deleted; // 字段 逻辑删除标记
}
