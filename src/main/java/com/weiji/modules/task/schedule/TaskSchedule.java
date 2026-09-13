package com.weiji.modules.task.schedule; // 包 com.weiji.modules.task.schedule

import com.weiji.modules.task.service.TaskService; // 本仓类 TaskService
import lombok.RequiredArgsConstructor; // Lombok 样板代码生成
import lombok.extern.slf4j.Slf4j; // Lombok 样板代码生成
import org.springframework.scheduling.annotation.Scheduled; // 导入 Scheduled
import org.springframework.stereotype.Component; // 通用组件

@Slf4j // Lombok 生成 log，走 Logback
@Component // 交给组件扫描注册
@RequiredArgsConstructor // Lombok：为 final 字段生成构造器注入
public class TaskSchedule { // 定义类 TaskSchedule

    private final TaskService taskService; // 构造注入 taskService

    @Scheduled(cron = "0 10 0 * * *") // 定时执行
    public void deferUnfinished() { // 方法 deferUnfinished
        taskService.deferUnfinished(); // 本行业务语句
        log.info("deferred unfinished tasks to today"); // 本行业务语句
    }
}
