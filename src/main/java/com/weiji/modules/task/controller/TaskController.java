package com.weiji.modules.task.controller; // HTTP 接口层

import com.weiji.common.result.Result; // 本仓类 Result
import com.weiji.framework.security.Currents; // 本仓类 Currents
import com.weiji.modules.task.entity.FocusSession; // 本仓类 FocusSession
import com.weiji.modules.task.entity.Task; // 本仓类 Task
import com.weiji.modules.task.entity.TaskTemplate; // 本仓类 TaskTemplate
import com.weiji.modules.task.service.TaskService; // 本仓类 TaskService
import lombok.Data; // Lombok 样板代码生成
import lombok.RequiredArgsConstructor; // Lombok 样板代码生成
import org.springframework.web.bind.annotation.GetMapping; // Web 映射注解
import org.springframework.web.bind.annotation.PathVariable; // Web 映射注解
import org.springframework.web.bind.annotation.PostMapping; // Web 映射注解
import org.springframework.web.bind.annotation.PutMapping; // Web 映射注解
import org.springframework.web.bind.annotation.RequestBody; // Web 映射注解
import org.springframework.web.bind.annotation.RequestMapping; // Web 映射注解
import org.springframework.web.bind.annotation.RequestParam; // Web 映射注解
import org.springframework.web.bind.annotation.RestController; // Web 映射注解

import java.util.List; // JDK 集合/工具
import java.util.Map; // JDK 集合/工具

@RestController // REST 控制器，返回值写入 HTTP 体
@RequestMapping("/api/task") // URL 前缀
@RequiredArgsConstructor // Lombok：为 final 字段生成构造器注入
public class TaskController { // 定义类 TaskController

    private final TaskService taskService; // 构造注入 taskService

    @GetMapping("/templates") // 处理 GET
    public Result<List<TaskTemplate>> templates() { // 方法 templates
        return Result.ok(taskService.templates(Currents.userId())); // 成功响应 code=0
    }

    @PostMapping("/templates") // 处理 POST
    public Result<TaskTemplate> saveTemplate(@RequestBody TaskTemplate template) { // 方法 saveTemplate
        return Result.ok(taskService.saveTemplate(Currents.userId(), template)); // 成功响应 code=0
    }

    @PostMapping("/templates/{id}/apply") // 处理 POST
    public Result<Task> apply(@PathVariable Long id) { // 方法 apply
        return Result.ok(taskService.applyTemplate(Currents.userId(), id)); // 成功响应 code=0
    }

    @PostMapping // 处理 POST
    public Result<Task> create(@RequestBody Task task) { // 方法 create
        return Result.ok(taskService.createTask(Currents.userId(), task)); // 成功响应 code=0
    }

    @GetMapping // 处理 GET
    public Result<List<Task>> list(@RequestParam(required = false) Integer status, // 赋值或调用
                                   @RequestParam(required = false) String category) { // 查询参数
        return Result.ok(taskService.listTasks(Currents.userId(), status, category)); // 成功响应 code=0
    }

    @PutMapping // 处理 PUT
    public Result<Task> update(@RequestBody Task task) { // 方法 update
        return Result.ok(taskService.updateTask(Currents.userId(), task)); // 成功响应 code=0
    }

    @PostMapping("/{id}/buffer") // 处理 POST
    public Result<Void> buffer(@PathVariable Long id) { // 方法 buffer
        taskService.buffer(Currents.userId(), id); // 取当前登录用户 ID
        return Result.ok(); // 成功响应 code=0
    }

    @PostMapping("/expire") // 处理 POST
    public Result<Void> expire(@RequestBody IdsRequest request) { // 方法 expire
        taskService.expireBatch(Currents.userId(), request.getIds()); // 取当前登录用户 ID
        return Result.ok(); // 成功响应 code=0
    }

    @PostMapping("/import") // 处理 POST
    public Result<List<Task>> importTasks(@RequestBody List<Task> tasks) { // 方法 importTasks
        return Result.ok(taskService.importTasks(Currents.userId(), tasks)); // 成功响应 code=0
    }

    @PostMapping("/match") // 处理 POST
    public Result<List<Task>> match(@RequestBody MatchRequest request) { // 方法 match
        return Result.ok(taskService.match(Currents.userId(), request.getIdleMinutes(), // 成功响应 code=0
                request.getSceneCode(), request.getCategory(), request.getEnergyStatus())); // 本行业务语句
    }

    @PostMapping("/preload") // 处理 POST
    public Result<List<Task>> preload(@RequestBody(required = false) MatchRequest request) { // 方法 preload
        Integer idle = request == null ? 30 : request.getIdleMinutes(); // 赋值或调用
        String scene = request == null ? null : request.getSceneCode(); // 赋值或调用
        return Result.ok(taskService.preload(Currents.userId(), idle, scene)); // 成功响应 code=0
    }

    @PostMapping("/{id}/complete") // 处理 POST
    public Result<Task> complete(@PathVariable Long id, @RequestBody(required = false) CompleteRequest request) { // 方法 complete
        Integer delay = request == null ? null : request.getStartDelaySeconds(); // 赋值或调用
        return Result.ok(taskService.complete(Currents.userId(), id, delay)); // 成功响应 code=0
    }

    @PostMapping("/{id}/skip") // 处理 POST
    public Result<Task> skip(@PathVariable Long id) { // 方法 skip
        return Result.ok(taskService.skip(Currents.userId(), id)); // 成功响应 code=0
    }

    @GetMapping("/delay-curve") // 处理 GET
    public Result<Map<String, Object>> delayCurve() { // 方法 delayCurve
        return Result.ok(taskService.delayCurve(Currents.userId())); // 成功响应 code=0
    }

    @Data // Lombok：getter/setter/equals/hashCode
    public static class MatchRequest { // 定义类 MatchRequest
        private Integer idleMinutes; // 字段 idleMinutes
        private String sceneCode; // 字段 sceneCode
        private String category; // 字段 category
        private String energyStatus; // 字段 energyStatus
    }

    @Data // Lombok：getter/setter/equals/hashCode
    public static class IdsRequest { // 定义类 IdsRequest
        private List<Long> ids; // 字段 ids
    }

    @Data // Lombok：getter/setter/equals/hashCode
    public static class CompleteRequest { // 定义类 CompleteRequest
        private Integer startDelaySeconds; // 字段 startDelaySeconds
    }
}
