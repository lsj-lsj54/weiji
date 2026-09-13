package com.weiji.modules.task.service; // 业务接口

import com.weiji.modules.task.entity.FocusSession; // 本仓类 FocusSession
import com.weiji.modules.task.entity.Task; // 本仓类 Task
import com.weiji.modules.task.entity.TaskTemplate; // 本仓类 TaskTemplate

import java.util.List; // JDK 集合/工具
import java.util.Map; // JDK 集合/工具

public interface TaskService { // 定义接口 TaskService

    List<TaskTemplate> templates(Long userId); // 本行业务语句

    TaskTemplate saveTemplate(Long userId, TaskTemplate template); // 本行业务语句

    Task applyTemplate(Long userId, Long templateId); // 本行业务语句

    Task createTask(Long userId, Task task); // 本行业务语句

    List<Task> listTasks(Long userId, Integer status, String category); // 本行业务语句

    Task updateTask(Long userId, Task task); // 本行业务语句

    void buffer(Long userId, Long taskId); // 本行业务语句

    void expireBatch(Long userId, List<Long> ids); // 本行业务语句

    List<Task> importTasks(Long userId, List<Task> tasks); // 本行业务语句

    List<Task> match(Long userId, Integer idleMinutes, String sceneCode, String category, String energyStatus); // 本行业务语句

    List<Task> preload(Long userId, Integer idleMinutes, String sceneCode); // 本行业务语句

    Task complete(Long userId, Long taskId, Integer startDelaySeconds); // 本行业务语句

    Task skip(Long userId, Long taskId); // 本行业务语句

    FocusSession startFocus(Long userId, FocusSession req); // 本行业务语句

    FocusSession pauseFocus(Long userId, Long sessionId); // 本行业务语句

    FocusSession resumeFocus(Long userId, Long sessionId); // 本行业务语句

    FocusSession finishFocus(Long userId, Long sessionId, Long clientEndTs, String remark); // 本行业务语句

    void abandonFocus(Long userId, Long sessionId); // 本行业务语句

    FocusSession liveFocus(Long userId); // 本行业务语句

    void deferUnfinished(); // 本行业务语句

    Map<String, Object> delayCurve(Long userId); // 本行业务语句
}
