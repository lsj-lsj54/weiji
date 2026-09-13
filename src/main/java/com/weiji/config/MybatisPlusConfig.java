package com.weiji.config; // Spring 配置包

import com.baomidou.mybatisplus.annotation.DbType; // MyBatis-Plus
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler; // MyBatis-Plus
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor; // MyBatis-Plus
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor; // MyBatis-Plus
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor; // MyBatis-Plus
import org.apache.ibatis.reflection.MetaObject; // 导入 MetaObject
import org.springframework.context.annotation.Bean; // 注册 Bean
import org.springframework.context.annotation.Configuration; // 配置类
import org.springframework.stereotype.Component; // 通用组件

import java.time.LocalDateTime; // 日期时间

@Configuration // 配置类，可声明 @Bean
public class MybatisPlusConfig { // 定义类 MybatisPlusConfig

    @Bean // 把返回值放进 Spring 容器
    public MybatisPlusInterceptor mybatisPlusInterceptor() { // 方法 mybatisPlusInterceptor
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor(); // 赋值或调用
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL)); // 分页插件，方言 MySQL
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor()); // 乐观锁插件
        return interceptor; // 返回结果
    }

    @Component // 交给组件扫描注册
    public static class AutoFillMetaObjectHandler implements MetaObjectHandler { // 定义类 AutoFillMetaObjectHandler，实现接口

        @Override // 覆盖父类/接口方法
        public void insertFill(MetaObject metaObject) { // 方法 insertFill
            LocalDateTime now = LocalDateTime.now(); // 赋值或调用
            this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now); // 插入时自动填时间
            this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now); // 插入时自动填时间
        }

        @Override // 覆盖父类/接口方法
        public void updateFill(MetaObject metaObject) { // 方法 updateFill
            this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now()); // 更新时自动填 updatedAt
        }
    }
}
