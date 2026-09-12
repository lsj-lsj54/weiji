package com.weiji.modules.task.service;

import com.weiji.modules.task.entity.FocusSession;
import com.weiji.modules.task.entity.Task;
import com.weiji.modules.task.entity.TaskTemplate;

import java.util.List;
import java.util.Map;

public interface TaskService {

    List<TaskTemplate> templates(Long userId);

    TaskTemplate saveTemplate(Long userId, TaskTemplate template);

    Task applyTemplate(Long userId, Long templateId);

    Task createTask(Long userId, Task task);

    List<Task> listTasks(Long userId, Integer status, String category);

    Task updateTask(Long userId, Task task);

    void buffer(Long userId, Long taskId);

    void expireBatch(Long userId, List<Long> ids);

    List<Task> importTasks(Long userId, List<Task> tasks);

    List<Task> match(Long userId, Integer idleMinutes, String sceneCode, String category, String energyStatus);

    List<Task> preload(Long userId, Integer idleMinutes, String sceneCode);

    Task complete(Long userId, Long taskId, Integer startDelaySeconds);

    Task skip(Long userId, Long taskId);

    FocusSession startFocus(Long userId, FocusSession req);

    FocusSession pauseFocus(Long userId, Long sessionId);

    FocusSession resumeFocus(Long userId, Long sessionId);

    FocusSession finishFocus(Long userId, Long sessionId, Long clientEndTs, String remark);

    void abandonFocus(Long userId, Long sessionId);

    FocusSession liveFocus(Long userId);

    void deferUnfinished();

    Map<String, Object> delayCurve(Long userId);
}
