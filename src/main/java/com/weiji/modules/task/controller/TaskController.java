package com.weiji.modules.task.controller;

import com.weiji.common.result.Result;
import com.weiji.framework.security.Currents;
import com.weiji.modules.task.entity.FocusSession;
import com.weiji.modules.task.entity.Task;
import com.weiji.modules.task.entity.TaskTemplate;
import com.weiji.modules.task.service.TaskService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/templates")
    public Result<List<TaskTemplate>> templates() {
        return Result.ok(taskService.templates(Currents.userId()));
    }

    @PostMapping("/templates")
    public Result<TaskTemplate> saveTemplate(@RequestBody TaskTemplate template) {
        return Result.ok(taskService.saveTemplate(Currents.userId(), template));
    }

    @PostMapping("/templates/{id}/apply")
    public Result<Task> apply(@PathVariable Long id) {
        return Result.ok(taskService.applyTemplate(Currents.userId(), id));
    }

    @PostMapping
    public Result<Task> create(@RequestBody Task task) {
        return Result.ok(taskService.createTask(Currents.userId(), task));
    }

    @GetMapping
    public Result<List<Task>> list(@RequestParam(required = false) Integer status,
                                   @RequestParam(required = false) String category) {
        return Result.ok(taskService.listTasks(Currents.userId(), status, category));
    }

    @PutMapping
    public Result<Task> update(@RequestBody Task task) {
        return Result.ok(taskService.updateTask(Currents.userId(), task));
    }

    @PostMapping("/{id}/buffer")
    public Result<Void> buffer(@PathVariable Long id) {
        taskService.buffer(Currents.userId(), id);
        return Result.ok();
    }

    @PostMapping("/expire")
    public Result<Void> expire(@RequestBody IdsRequest request) {
        taskService.expireBatch(Currents.userId(), request.getIds());
        return Result.ok();
    }

    @PostMapping("/import")
    public Result<List<Task>> importTasks(@RequestBody List<Task> tasks) {
        return Result.ok(taskService.importTasks(Currents.userId(), tasks));
    }

    @PostMapping("/match")
    public Result<List<Task>> match(@RequestBody MatchRequest request) {
        return Result.ok(taskService.match(Currents.userId(), request.getIdleMinutes(),
                request.getSceneCode(), request.getCategory(), request.getEnergyStatus()));
    }

    @PostMapping("/preload")
    public Result<List<Task>> preload(@RequestBody(required = false) MatchRequest request) {
        Integer idle = request == null ? 30 : request.getIdleMinutes();
        String scene = request == null ? null : request.getSceneCode();
        return Result.ok(taskService.preload(Currents.userId(), idle, scene));
    }

    @PostMapping("/{id}/complete")
    public Result<Task> complete(@PathVariable Long id, @RequestBody(required = false) CompleteRequest request) {
        Integer delay = request == null ? null : request.getStartDelaySeconds();
        return Result.ok(taskService.complete(Currents.userId(), id, delay));
    }

    @PostMapping("/{id}/skip")
    public Result<Task> skip(@PathVariable Long id) {
        return Result.ok(taskService.skip(Currents.userId(), id));
    }

    @GetMapping("/delay-curve")
    public Result<Map<String, Object>> delayCurve() {
        return Result.ok(taskService.delayCurve(Currents.userId()));
    }

    @Data
    public static class MatchRequest {
        private Integer idleMinutes;
        private String sceneCode;
        private String category;
        private String energyStatus;
    }

    @Data
    public static class IdsRequest {
        private List<Long> ids;
    }

    @Data
    public static class CompleteRequest {
        private Integer startDelaySeconds;
    }
}
