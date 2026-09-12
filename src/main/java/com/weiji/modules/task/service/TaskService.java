package com.weiji.modules.task.service;

import com.weiji.modules.task.entity.FocusSession;
import com.weiji.modules.task.entity.Task;

import java.util.List;

public interface TaskService {

    List<Task> matchTasks(Long userId, Integer idleMinutes, String sceneCode, String energyStatus);

    Long startFocus(Long userId, String title);

    void finishFocus(Long sessionId, Long clientEndTs);

    FocusSession getFocus(Long sessionId);
}
