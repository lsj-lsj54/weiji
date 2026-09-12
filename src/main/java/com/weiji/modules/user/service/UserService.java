package com.weiji.modules.user.service;

import com.weiji.modules.user.dto.UpdateProfileRequest;
import com.weiji.modules.user.entity.UserGoal;
import com.weiji.modules.user.entity.UserPreference;
import com.weiji.modules.user.vo.UserVO;

import java.util.List;

public interface UserService {

    UserVO currentUser();

    UserVO updateProfile(UpdateProfileRequest request);

    UserPreference preference();

    UserPreference updatePreference(UserPreference preference);

    UserGoal createGoal(UserGoal goal);

    List<UserGoal> goals();

    List<com.weiji.modules.task.entity.Task> decomposeGoal(Long goalId, Integer chunkMinutes);

    UserVO searchByPhone(String phone);
}
