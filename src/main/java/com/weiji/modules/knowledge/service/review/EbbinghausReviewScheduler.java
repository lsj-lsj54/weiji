package com.weiji.modules.knowledge.service.review; // 业务接口

import org.springframework.stereotype.Component; // 通用组件

import java.util.List; // JDK 集合/工具

@Component // 交给组件扫描注册
public class EbbinghausReviewScheduler { // 定义类 EbbinghausReviewScheduler

    private static final List<Integer> INTERVALS = List.of(1, 2, 4, 7, 15); // 成员字段

    public int nextInterval(int current, boolean remembered) { // 方法 nextInterval
        if (!remembered) { // 条件判断
            return 1; // 返回结果
        }
        for (int i = 0; i < INTERVALS.size(); i++) { // 循环
            if (current < INTERVALS.get(i)) { // 条件判断
                return INTERVALS.get(i); // 返回结果
            }
            if (current == INTERVALS.get(i) && i < INTERVALS.size() - 1) { // 条件判断
                return INTERVALS.get(i + 1); // 返回结果
            }
        }
        return 15; // 返回结果
    }
}
