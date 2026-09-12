package com.weiji.modules.user.controller;

import com.weiji.common.result.Result;
import com.weiji.modules.task.entity.Task;
import com.weiji.modules.user.dto.UpdateProfileRequest;
import com.weiji.modules.user.entity.UserGoal;
import com.weiji.modules.user.entity.UserPreference;
import com.weiji.modules.user.service.UserService;
import com.weiji.modules.user.vo.UserVO;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.ok(userService.currentUser());
    }

    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return Result.ok(userService.updateProfile(request));
    }

    @GetMapping("/preference")
    public Result<UserPreference> preference() {
        return Result.ok(userService.preference());
    }

    @PutMapping("/preference")
    public Result<UserPreference> updatePreference(@RequestBody UserPreference preference) {
        return Result.ok(userService.updatePreference(preference));
    }

    @PostMapping("/goals")
    public Result<UserGoal> createGoal(@RequestBody UserGoal goal) {
        return Result.ok(userService.createGoal(goal));
    }

    @GetMapping("/goals")
    public Result<List<UserGoal>> goals() {
        return Result.ok(userService.goals());
    }

    @PostMapping("/goals/{id}/decompose")
    public Result<List<Task>> decompose(@PathVariable Long id, @RequestBody(required = false) DecomposeRequest request) {
        Integer chunk = request == null ? null : request.getChunkMinutes();
        return Result.ok(userService.decomposeGoal(id, chunk));
    }

    @Data
    public static class DecomposeRequest {
        private Integer chunkMinutes;
    }
}
