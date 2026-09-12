package com.weiji.modules.task.service.matcher;

import com.weiji.modules.task.entity.Task;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RuleTaskMatcherTest {

    @Test
    void packsByIdleMinutesAndPriority() {
        RuleTaskMatcher matcher = new RuleTaskMatcher();
        Task urgent = task("紧急", 0, 10, "学习");
        Task longTask = task("长任务", 1, 40, "工作");
        Task easy = task("轻松", 2, 5, "生活");
        List<Task> result = matcher.match(List.of(longTask, easy, urgent), 20, null, null, "ENERGETIC",
                LocalTime.of(9, 0), false);
        assertEquals(2, result.size());
        assertEquals("紧急", result.get(0).getTitle());
    }

    private Task task(String title, int priority, int minutes, String category) {
        Task t = new Task();
        t.setTitle(title);
        t.setPriority(priority);
        t.setDurationMinutes(minutes);
        t.setCategory(category);
        t.setStatus(0);
        return t;
    }
}
