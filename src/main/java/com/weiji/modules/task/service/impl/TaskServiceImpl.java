package com.weiji.modules.task.service.impl;

import com.weiji.modules.task.entity.FocusSession;
import com.weiji.modules.task.entity.Task;
import com.weiji.modules.task.service.TaskService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    @Override
    public List<Task> matchTasks(Long userId, Integer idleMinutes, String sceneCode, String energyStatus) {
        throw new UnsupportedOperationException("任务匹配将在后续迭代实现");
    }

    @Override
    public Long startFocus(Long userId, String title) {
        throw new UnsupportedOperationException("专注计时将在后续迭代实现");
    }

    @Override
    public void finishFocus(Long sessionId, Long clientEndTs) {
        throw new UnsupportedOperationException("专注计时将在后续迭代实现");
    }

    @Override
    public FocusSession getFocus(Long sessionId) {
        throw new UnsupportedOperationException("专注计时将在后续迭代实现");
    }
}
