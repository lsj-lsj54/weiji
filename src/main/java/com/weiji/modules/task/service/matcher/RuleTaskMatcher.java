package com.weiji.modules.task.service.matcher;

import com.weiji.modules.task.entity.Task;
import com.weiji.modules.user.entity.UserPreference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 时长 + 场景 + 优先级 + 时段 + 用户精力状态。
 */
@Component
public class RuleTaskMatcher {

    public List<Task> match(List<Task> pool, int idleMinutes, String sceneCode, String category,
                            String energyStatus, LocalTime now, boolean weekend) {
        List<Task> filtered = pool.stream()
                .filter(t -> t.getDurationMinutes() != null && t.getDurationMinutes() <= idleMinutes)
                .filter(t -> StringUtils.isBlank(sceneCode) || StringUtils.isBlank(t.getSceneCode())
                        || sceneCode.equalsIgnoreCase(t.getSceneCode()))
                .filter(t -> StringUtils.isBlank(category) || category.equals(t.getCategory()))
                .sorted(comparator(energyStatus, now, weekend))
                .collect(Collectors.toList());
        List<Task> packed = new ArrayList<>();
        int remain = idleMinutes;
        for (Task task : filtered) {
            if (task.getDurationMinutes() <= remain) {
                packed.add(task);
                remain -= task.getDurationMinutes();
            }
            if (remain <= 0) {
                break;
            }
        }
        return packed;
    }

    public boolean isRestDay(UserPreference pref, LocalDate date) {
        if (pref == null || StringUtils.isBlank(pref.getRestDays())) {
            return false;
        }
        int mark = date.getDayOfWeek().getValue() % 7;
        return pref.getRestDays().contains(String.valueOf(mark));
    }

    public boolean inDnd(UserPreference pref, LocalTime time) {
        if (pref == null || StringUtils.isBlank(pref.getDndPeriods())) {
            return false;
        }
        String raw = pref.getDndPeriods();
        for (String part : raw.replace("[", "").replace("]", "").replace("\"", "").split(",")) {
            String span = part.trim();
            if (!span.contains("-")) {
                continue;
            }
            String[] se = span.split("-");
            if (se.length != 2) {
                continue;
            }
            LocalTime start = LocalTime.parse(se[0].trim());
            LocalTime end = LocalTime.parse(se[1].trim());
            if (!start.isAfter(end)) {
                if (!time.isBefore(start) && !time.isAfter(end)) {
                    return true;
                }
            } else if (!time.isBefore(start) || !time.isAfter(end)) {
                return true;
            }
        }
        return false;
    }

    private Comparator<Task> comparator(String energy, LocalTime now, boolean weekend) {
        return Comparator
                .comparing((Task t) -> t.getReviewNoteId() == null ? 1 : 0)
                .thenComparingInt(t -> slotBoost(t, now, weekend))
                .thenComparingInt(t -> energyBoost(t, energy))
                .thenComparingInt(t -> t.getPriority() == null ? 1 : t.getPriority())
                .thenComparing(Task::getDurationMinutes, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private int slotBoost(Task task, LocalTime now, boolean weekend) {
        String cat = StringUtils.defaultString(task.getCategory());
        if (now.getHour() < 12 && "学习".equals(cat)) {
            return 0;
        }
        if (now.getHour() >= 18 && ("生活".equals(cat) || "自我提升".equals(cat))) {
            return 0;
        }
        if (weekend && "生活".equals(cat)) {
            return 0;
        }
        return 1;
    }

    private int energyBoost(Task task, String energy) {
        int duration = task.getDurationMinutes() == null ? 15 : task.getDurationMinutes();
        int priority = task.getPriority() == null ? 1 : task.getPriority();
        if ("TIRED".equals(energy)) {
            return duration <= 15 ? 0 : 1;
        }
        if ("SLACKING".equals(energy)) {
            return priority >= 2 || duration <= 10 ? 0 : 1;
        }
        return duration >= 15 && priority <= 1 ? 0 : 1;
    }
}
