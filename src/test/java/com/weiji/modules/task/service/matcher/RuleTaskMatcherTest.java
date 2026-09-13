package com.weiji.modules.task.service.matcher; // 业务接口

import com.weiji.modules.task.entity.Task; // 本仓类 Task
import org.junit.jupiter.api.Test; // JUnit 测试

import java.time.LocalTime; // 日期时间
import java.util.List; // JDK 集合/工具

import static org.junit.jupiter.api.Assertions.assertEquals; // JUnit 测试

class RuleTaskMatcherTest { // 定义类 RuleTaskMatcherTest

    @Test // JUnit 测试方法
    void packsByIdleMinutesAndPriority() { // 开始代码块
        RuleTaskMatcher matcher = new RuleTaskMatcher(); // 赋值或调用
        Task urgent = task("紧急", 0, 10, "学习"); // 赋值或调用
        Task longTask = task("长任务", 1, 40, "工作"); // 赋值或调用
        Task easy = task("轻松", 2, 5, "生活"); // 赋值或调用
        List<Task> result = matcher.match(List.of(longTask, easy, urgent), 20, null, null, "ENERGETIC", // 赋值或调用
                LocalTime.of(9, 0), false); // 本行业务语句
        assertEquals(2, result.size()); // 本行业务语句
        assertEquals("紧急", result.get(0).getTitle()); // 本行业务语句
    }

    private Task task(String title, int priority, int minutes, String category) { // 方法 task
        Task t = new Task(); // 赋值或调用
        t.setTitle(title); // 本行业务语句
        t.setPriority(priority); // 本行业务语句
        t.setDurationMinutes(minutes); // 本行业务语句
        t.setCategory(category); // 本行业务语句
        t.setStatus(0); // 本行业务语句
        return t; // 返回结果
    }
}
