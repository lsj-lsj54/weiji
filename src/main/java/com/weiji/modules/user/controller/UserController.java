package com.weiji.modules.user.controller;

import com.weiji.common.result.Result;
import com.weiji.modules.user.dto.UpdateProfileRequest;
import com.weiji.modules.user.service.UserService;
import com.weiji.modules.user.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
