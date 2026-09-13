package com.weiji.modules.task.controller; // HTTP 接口层

import com.weiji.common.result.Result; // 本仓类 Result
import com.weiji.framework.security.Currents; // 本仓类 Currents
import com.weiji.modules.task.entity.FocusSession; // 本仓类 FocusSession
import com.weiji.modules.task.service.TaskService; // 本仓类 TaskService
import lombok.Data; // Lombok 样板代码生成
import lombok.RequiredArgsConstructor; // Lombok 样板代码生成
import org.springframework.web.bind.annotation.GetMapping; // Web 映射注解
import org.springframework.web.bind.annotation.PathVariable; // Web 映射注解
import org.springframework.web.bind.annotation.PostMapping; // Web 映射注解
import org.springframework.web.bind.annotation.RequestBody; // Web 映射注解
import org.springframework.web.bind.annotation.RequestMapping; // Web 映射注解
import org.springframework.web.bind.annotation.RestController; // Web 映射注解

@RestController // REST 控制器，返回值写入 HTTP 体
@RequestMapping("/api/focus") // URL 前缀
@RequiredArgsConstructor // Lombok：为 final 字段生成构造器注入
public class FocusController { // 定义类 FocusController

    private final TaskService taskService; // 构造注入 taskService

    @PostMapping("/start") // 处理 POST
    public Result<FocusSession> start(@RequestBody FocusSession session) { // 方法 start
        return Result.ok(taskService.startFocus(Currents.userId(), session)); // 成功响应 code=0
    }

    @PostMapping("/{id}/pause") // 处理 POST
    public Result<FocusSession> pause(@PathVariable Long id) { // 方法 pause
        return Result.ok(taskService.pauseFocus(Currents.userId(), id)); // 成功响应 code=0
    }

    @PostMapping("/{id}/resume") // 处理 POST
    public Result<FocusSession> resume(@PathVariable Long id) { // 方法 resume
        return Result.ok(taskService.resumeFocus(Currents.userId(), id)); // 成功响应 code=0
    }

    @PostMapping("/{id}/finish") // 处理 POST
    public Result<FocusSession> finish(@PathVariable Long id, @RequestBody(required = false) FinishRequest request) { // 方法 finish
        Long clientEnd = request == null ? null : request.getClientEndTs(); // 赋值或调用
        String remark = request == null ? null : request.getRemark(); // 赋值或调用
        return Result.ok(taskService.finishFocus(Currents.userId(), id, clientEnd, remark)); // 成功响应 code=0
    }

    @PostMapping("/{id}/abandon") // 处理 POST
    public Result<Void> abandon(@PathVariable Long id) { // 方法 abandon
        taskService.abandonFocus(Currents.userId(), id); // 取当前登录用户 ID
        return Result.ok(); // 成功响应 code=0
    }

    @GetMapping("/live") // 处理 GET
    public Result<FocusSession> live() { // 方法 live
        return Result.ok(taskService.liveFocus(Currents.userId())); // 成功响应 code=0
    }

    @Data // Lombok：getter/setter/equals/hashCode
    public static class FinishRequest { // 定义类 FinishRequest
        private Long clientEndTs; // 字段 clientEndTs
        private String remark; // 字段 remark
    }
}
