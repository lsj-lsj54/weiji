package com.weiji.modules.user.controller; // HTTP 接口层

import com.weiji.common.result.Result; // 本仓类 Result
import com.weiji.modules.task.entity.Task; // 本仓类 Task
import com.weiji.modules.user.dto.UpdateProfileRequest; // 本仓类 UpdateProfileRequest
import com.weiji.modules.user.entity.UserGoal; // 本仓类 UserGoal
import com.weiji.modules.user.entity.UserPreference; // 本仓类 UserPreference
import com.weiji.modules.user.service.UserService; // 本仓类 UserService
import com.weiji.modules.user.vo.UserVO; // 本仓类 UserVO
import jakarta.validation.Valid; // Bean Validation
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

@RestController // REST 控制器，返回值写入 HTTP 体
@RequestMapping("/api/user") // URL 前缀
@RequiredArgsConstructor // Lombok：为 final 字段生成构造器注入
public class UserController { // 定义类 UserController

    private final UserService userService; // 构造注入 userService

    @GetMapping("/me") // 处理 GET
    public Result<UserVO> me() { // 方法 me
        return Result.ok(userService.currentUser()); // 成功响应 code=0
    }

    @PutMapping("/profile") // 处理 PUT
    public Result<UserVO> updateProfile(@Valid @RequestBody UpdateProfileRequest request) { // 方法 updateProfile
        return Result.ok(userService.updateProfile(request)); // 成功响应 code=0
    }

    @GetMapping("/preference") // 处理 GET
    public Result<UserPreference> preference() { // 方法 preference
        return Result.ok(userService.preference()); // 成功响应 code=0
    }

    @PutMapping("/preference") // 处理 PUT
    public Result<UserPreference> updatePreference(@RequestBody UserPreference preference) { // 方法 updatePreference
        return Result.ok(userService.updatePreference(preference)); // 成功响应 code=0
    }

    @PostMapping("/goals") // 处理 POST
    public Result<UserGoal> createGoal(@RequestBody UserGoal goal) { // 方法 createGoal
        return Result.ok(userService.createGoal(goal)); // 成功响应 code=0
    }

    @GetMapping("/goals") // 处理 GET
    public Result<List<UserGoal>> goals() { // 方法 goals
        return Result.ok(userService.goals()); // 成功响应 code=0
    }

    @GetMapping("/search") // 处理 GET
    public Result<UserVO> search(@RequestParam String phone) { // 方法 search
        return Result.ok(userService.searchByPhone(phone)); // 成功响应 code=0
    }

    @PostMapping("/goals/{id}/decompose") // 处理 POST
    public Result<List<Task>> decompose(@PathVariable Long id, @RequestBody(required = false) DecomposeRequest request) { // 方法 decompose
        Integer chunk = request == null ? null : request.getChunkMinutes(); // 赋值或调用
        return Result.ok(userService.decomposeGoal(id, chunk)); // 成功响应 code=0
    }

    @Data // Lombok：getter/setter/equals/hashCode
    public static class DecomposeRequest { // 定义类 DecomposeRequest
        private Integer chunkMinutes; // 字段 chunkMinutes
    }
}
