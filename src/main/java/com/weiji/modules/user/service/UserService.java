package com.weiji.modules.user.service; // 业务接口

import com.weiji.modules.user.dto.UpdateProfileRequest; // 本仓类 UpdateProfileRequest
import com.weiji.modules.user.entity.UserGoal; // 本仓类 UserGoal
import com.weiji.modules.user.entity.UserPreference; // 本仓类 UserPreference
import com.weiji.modules.user.vo.UserVO; // 本仓类 UserVO

import java.util.List; // JDK 集合/工具

public interface UserService { // 定义接口 UserService

    UserVO currentUser(); // 本行业务语句

    UserVO getUser(Long userId); // 本行业务语句

    UserVO updateProfile(UpdateProfileRequest request); // 本行业务语句

    UserPreference preference(); // 本行业务语句

    UserPreference updatePreference(UserPreference preference); // 本行业务语句

    UserGoal createGoal(UserGoal goal); // 本行业务语句

    List<UserGoal> goals(); // 本行业务语句

    List<com.weiji.modules.task.entity.Task> decomposeGoal(Long goalId, Integer chunkMinutes); // 本行业务语句

    UserVO searchByPhone(String phone); // 本行业务语句
}
