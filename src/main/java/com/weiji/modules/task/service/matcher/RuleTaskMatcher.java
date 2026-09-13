package com.weiji.modules.task.service.matcher; // 业务接口

import com.weiji.modules.task.entity.Task; // 本仓类 Task
import com.weiji.modules.user.entity.UserPreference; // 本仓类 UserPreference
import org.apache.commons.lang3.StringUtils; // 字符串工具
import org.springframework.stereotype.Component; // 通用组件

import java.time.LocalDate; // 日期时间
import java.time.LocalTime; // 日期时间
import java.util.ArrayList; // JDK 集合/工具
import java.util.Comparator; // JDK 集合/工具
import java.util.List; // JDK 集合/工具
import java.util.stream.Collectors; // JDK 集合/工具

/**
 * 时长 + 场景 + 优先级 + 时段 + 用户精力状态。
 */
@Component // 交给组件扫描注册
public class RuleTaskMatcher { // 定义类 RuleTaskMatcher

    public List<Task> match(List<Task> pool, int idleMinutes, String sceneCode, String category, // 本行业务语句
                            String energyStatus, LocalTime now, boolean weekend) { // 开始代码块
        List<Task> filtered = pool.stream() // 赋值或调用
                .filter(t -> t.getDurationMinutes() != null && t.getDurationMinutes() <= idleMinutes) // 赋值或调用
                .filter(t -> StringUtils.isBlank(sceneCode) || StringUtils.isBlank(t.getSceneCode()) // 本行业务语句
                        || sceneCode.equalsIgnoreCase(t.getSceneCode())) // 本行业务语句
                .filter(t -> StringUtils.isBlank(category) || category.equals(t.getCategory())) // 本行业务语句
                .sorted(comparator(energyStatus, now, weekend)) // 本行业务语句
                .collect(Collectors.toList()); // 本行业务语句
        List<Task> packed = new ArrayList<>(); // 赋值或调用
        int remain = idleMinutes; // 赋值或调用
        for (Task task : filtered) { // 循环
            if (task.getDurationMinutes() <= remain) { // 条件判断
                packed.add(task); // 本行业务语句
                remain -= task.getDurationMinutes(); // 赋值或调用
            }
            if (remain <= 0) { // 条件判断
                break; // 本行业务语句
            }
        }
        return packed; // 返回结果
    }

    public boolean isRestDay(UserPreference pref, LocalDate date) { // 方法 isRestDay
        if (pref == null || StringUtils.isBlank(pref.getRestDays())) { // 条件判断
            return false; // 返回 false
        }
        int mark = date.getDayOfWeek().getValue() % 7; // 赋值或调用
        return pref.getRestDays().contains(String.valueOf(mark)); // 返回结果
    }

    public boolean inDnd(UserPreference pref, LocalTime time) { // 方法 inDnd
        if (pref == null || StringUtils.isBlank(pref.getDndPeriods())) { // 条件判断
            return false; // 返回 false
        }
        String raw = pref.getDndPeriods(); // 赋值或调用
        for (String part : raw.replace("[", "").replace("]", "").replace("\"", "").split(",")) { // 循环
            String span = part.trim(); // 赋值或调用
            if (!span.contains("-")) { // 条件判断
                continue; // 本行业务语句
            }
            String[] se = span.split("-"); // 赋值或调用
            if (se.length != 2) { // 条件判断
                continue; // 本行业务语句
            }
            LocalTime start = LocalTime.parse(se[0].trim()); // 赋值或调用
            LocalTime end = LocalTime.parse(se[1].trim()); // 赋值或调用
            if (!start.isAfter(end)) { // 条件判断
                if (!time.isBefore(start) && !time.isAfter(end)) { // 条件判断
                    return true; // 返回 true
                }
            } else if (!time.isBefore(start) || !time.isAfter(end)) { // 开始代码块
                return true; // 返回 true
            }
        }
        return false; // 返回 false
    }

    private Comparator<Task> comparator(String energy, LocalTime now, boolean weekend) { // 方法 comparator
        return Comparator // 返回结果
                .comparing((Task t) -> t.getReviewNoteId() == null ? 1 : 0) // 赋值或调用
                .thenComparingInt(t -> slotBoost(t, now, weekend)) // 本行业务语句
                .thenComparingInt(t -> energyBoost(t, energy)) // 本行业务语句
                .thenComparingInt(t -> t.getPriority() == null ? 1 : t.getPriority()) // 赋值或调用
                .thenComparing(Task::getDurationMinutes, Comparator.nullsLast(Comparator.reverseOrder())); // 本行业务语句
    }

    private int slotBoost(Task task, LocalTime now, boolean weekend) { // 方法 slotBoost
        String cat = StringUtils.defaultString(task.getCategory()); // 赋值或调用
        if (now.getHour() < 12 && "学习".equals(cat)) { // 条件判断
            return 0; // 返回结果
        }
        if (now.getHour() >= 18 && ("生活".equals(cat) || "自我提升".equals(cat))) { // 条件判断
            return 0; // 返回结果
        }
        if (weekend && "生活".equals(cat)) { // 条件判断
            return 0; // 返回结果
        }
        return 1; // 返回结果
    }

    private int energyBoost(Task task, String energy) { // 方法 energyBoost
        int duration = task.getDurationMinutes() == null ? 15 : task.getDurationMinutes(); // 赋值或调用
        int priority = task.getPriority() == null ? 1 : task.getPriority(); // 赋值或调用
        if ("TIRED".equals(energy)) { // 条件判断
            return duration <= 15 ? 0 : 1; // 返回结果
        }
        if ("SLACKING".equals(energy)) { // 条件判断
            return priority >= 2 || duration <= 10 ? 0 : 1; // 返回结果
        }
        return duration >= 15 && priority <= 1 ? 0 : 1; // 返回结果
    }
}
