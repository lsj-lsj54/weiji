package com.weiji.common.entity; // 公共包（结果/异常/常量）

import com.baomidou.mybatisplus.annotation.FieldFill; // MyBatis-Plus
import com.baomidou.mybatisplus.annotation.IdType; // MyBatis-Plus
import com.baomidou.mybatisplus.annotation.TableField; // MyBatis-Plus
import com.baomidou.mybatisplus.annotation.TableId; // MyBatis-Plus
import lombok.Data; // Lombok 样板代码生成

import java.time.LocalDateTime; // 日期时间

@Data // Lombok：getter/setter/equals/hashCode
public abstract class BaseEntity { // 定义类 BaseEntity

    @TableId(type = IdType.AUTO) // 主键及生成策略
    private Long id; // 字段 id

    @TableField(fill = FieldFill.INSERT) // 列映射或自动填充
    private LocalDateTime createdAt; // 字段 createdAt

    @TableField(fill = FieldFill.INSERT_UPDATE) // 列映射或自动填充
    private LocalDateTime updatedAt; // 字段 updatedAt
}
