package com.weiji.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.weiji.common.enums.ErrorCode;
import com.weiji.common.exception.BizException;
import com.weiji.framework.security.Currents;
import com.weiji.modules.task.entity.Task;
import com.weiji.modules.task.service.TaskService;
import com.weiji.modules.user.dto.UpdateProfileRequest;
import com.weiji.modules.user.entity.User;
import com.weiji.modules.user.entity.UserGoal;
import com.weiji.modules.user.entity.UserPreference;
import com.weiji.modules.user.mapper.UserGoalMapper;
import com.weiji.modules.user.mapper.UserMapper;
import com.weiji.modules.user.mapper.UserPreferenceMapper;
import com.weiji.modules.user.service.UserService;
import com.weiji.modules.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserPreferenceMapper userPreferenceMapper;
    private final UserGoalMapper userGoalMapper;
    private final TaskService taskService;

    @Override
    public UserVO currentUser() {
        return toVo(loadCurrent());
    }

    @Override
    public UserVO updateProfile(UpdateProfileRequest request) {
        User user = loadCurrent();
        if (StringUtils.isNotBlank(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (StringUtils.isNotBlank(request.getAvatar())) {
            user.setAvatar(request.getAvatar());
        }
        userMapper.updateById(user);
        return toVo(userMapper.selectById(user.getId()));
    }

    @Override
    public UserPreference preference() {
        Long userId = Currents.userId();
        UserPreference pref = userPreferenceMapper.selectOne(new LambdaQueryWrapper<UserPreference>()
                .eq(UserPreference::getUserId, userId));
        if (pref == null) {
            pref = new UserPreference();
            pref.setUserId(userId);
            pref.setEnergyStatus("ENERGETIC");
            userPreferenceMapper.insert(pref);
        }
        return pref;
    }

    @Override
    public UserPreference updatePreference(UserPreference preference) {
        UserPreference old = preference();
        preference.setId(old.getId());
        preference.setUserId(old.getUserId());
        userPreferenceMapper.updateById(preference);
        return userPreferenceMapper.selectById(old.getId());
    }

    @Override
    public UserGoal createGoal(UserGoal goal) {
        goal.setId(null);
        goal.setUserId(Currents.userId());
        if (goal.getStatus() == null) {
            goal.setStatus(0);
        }
        if (goal.getFinishedMinutes() == null) {
            goal.setFinishedMinutes(0);
        }
        userGoalMapper.insert(goal);
        return goal;
    }

    @Override
    public List<UserGoal> goals() {
        return userGoalMapper.selectList(new LambdaQueryWrapper<UserGoal>()
                .eq(UserGoal::getUserId, Currents.userId())
                .orderByDesc(UserGoal::getId));
    }

    @Override
    @Transactional
    public List<Task> decomposeGoal(Long goalId, Integer chunkMinutes) {
        Long userId = Currents.userId();
        UserGoal goal = userGoalMapper.selectById(goalId);
        if (goal == null || !userId.equals(goal.getUserId())) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        int chunk = chunkMinutes == null ? 15 : Math.min(30, Math.max(5, chunkMinutes));
        int total = goal.getTargetMinutes() == null || goal.getTargetMinutes() < chunk ? chunk : goal.getTargetMinutes();
        List<Task> created = new ArrayList<>();
        int order = 1;
        int remain = total;
        while (remain > 0) {
            int minutes = Math.min(chunk, remain);
            Task task = new Task();
            task.setTitle(goal.getTitle() + " · 第" + order + "段");
            task.setContent(goal.getDescription());
            task.setGoalId(goalId);
            task.setDurationMinutes(minutes);
            task.setPriority(1);
            task.setCategory("自我提升");
            task.setSourceType("GOAL");
            task.setSortOrder(order);
            created.add(taskService.createTask(userId, task));
            remain -= minutes;
            order++;
        }
        return created;
    }

    private User loadCurrent() {
        User user = userMapper.selectById(Currents.userId());
        if (user == null) {
            throw new BizException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private UserVO toVo(User user) {
        return UserVO.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .build();
    }
}
