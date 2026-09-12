package com.weiji.modules.task.schedule;

import com.weiji.modules.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskSchedule {

    private final TaskService taskService;

    @Scheduled(cron = "0 10 0 * * *")
    public void deferUnfinished() {
        taskService.deferUnfinished();
        log.info("deferred unfinished tasks to today");
    }
}
