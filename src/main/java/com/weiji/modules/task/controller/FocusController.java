package com.weiji.modules.task.controller;

import com.weiji.common.result.Result;
import com.weiji.framework.security.Currents;
import com.weiji.modules.task.entity.FocusSession;
import com.weiji.modules.task.service.TaskService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/focus")
@RequiredArgsConstructor
public class FocusController {

    private final TaskService taskService;

    @PostMapping("/start")
    public Result<FocusSession> start(@RequestBody FocusSession session) {
        return Result.ok(taskService.startFocus(Currents.userId(), session));
    }

    @PostMapping("/{id}/pause")
    public Result<FocusSession> pause(@PathVariable Long id) {
        return Result.ok(taskService.pauseFocus(Currents.userId(), id));
    }

    @PostMapping("/{id}/resume")
    public Result<FocusSession> resume(@PathVariable Long id) {
        return Result.ok(taskService.resumeFocus(Currents.userId(), id));
    }

    @PostMapping("/{id}/finish")
    public Result<FocusSession> finish(@PathVariable Long id, @RequestBody(required = false) FinishRequest request) {
        Long clientEnd = request == null ? null : request.getClientEndTs();
        String remark = request == null ? null : request.getRemark();
        return Result.ok(taskService.finishFocus(Currents.userId(), id, clientEnd, remark));
    }

    @PostMapping("/{id}/abandon")
    public Result<Void> abandon(@PathVariable Long id) {
        taskService.abandonFocus(Currents.userId(), id);
        return Result.ok();
    }

    @GetMapping("/live")
    public Result<FocusSession> live() {
        return Result.ok(taskService.liveFocus(Currents.userId()));
    }

    @Data
    public static class FinishRequest {
        private Long clientEndTs;
        private String remark;
    }
}
