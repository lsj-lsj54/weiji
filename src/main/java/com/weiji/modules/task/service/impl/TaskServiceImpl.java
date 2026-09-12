package com.weiji.modules.task.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.weiji.common.constant.CacheNames;
import com.weiji.common.constant.RedisKey;
import com.weiji.common.enums.ErrorCode;
import com.weiji.common.exception.BizException;
import com.weiji.framework.cache.MultiLevelCache;
import com.weiji.framework.redis.RedisUtils;
import com.weiji.modules.point.service.PointService;
import com.weiji.modules.task.entity.FocusSession;
import com.weiji.modules.task.entity.Task;
import com.weiji.modules.task.entity.TaskTemplate;
import com.weiji.modules.task.mapper.FocusSessionMapper;
import com.weiji.modules.task.mapper.TaskMapper;
import com.weiji.modules.task.mapper.TaskTemplateMapper;
import com.weiji.modules.task.service.TaskService;
import com.weiji.modules.task.service.matcher.RuleTaskMatcher;
import com.weiji.modules.user.entity.UserGoal;
import com.weiji.modules.user.entity.UserPreference;
import com.weiji.modules.user.mapper.UserGoalMapper;
import com.weiji.modules.user.mapper.UserPreferenceMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private static final int STATUS_TODO = 0;
    private static final int STATUS_DOING = 1;
    private static final int STATUS_DONE = 2;
    private static final int STATUS_BUFFER = 3;
    private static final int STATUS_EXPIRED = 4;

    private final TaskMapper taskMapper;
    private final TaskTemplateMapper taskTemplateMapper;
    private final FocusSessionMapper focusSessionMapper;
    private final UserPreferenceMapper userPreferenceMapper;
    private final UserGoalMapper userGoalMapper;
    private final RuleTaskMatcher matcher;
    private final PointService pointService;
    private final RedisUtils redisUtils;
    private final MultiLevelCache cache;

    @Override
    public List<TaskTemplate> templates(Long userId) {
        List<TaskTemplate> system = cache.getList(CacheNames.SYS_TEMPLATE, CacheNames.ALL, TaskTemplate.class,
                () -> taskTemplateMapper.selectList(new LambdaQueryWrapper<TaskTemplate>()
                        .isNull(TaskTemplate::getUserId)
                        .orderByAsc(TaskTemplate::getId)));
        List<TaskTemplate> mine = taskTemplateMapper.selectList(new LambdaQueryWrapper<TaskTemplate>()
                .eq(TaskTemplate::getUserId, userId)
                .orderByAsc(TaskTemplate::getId));
        List<TaskTemplate> result = new ArrayList<>(system.size() + mine.size());
        result.addAll(system);
        result.addAll(mine);
        return result;
    }

    @Override
    public TaskTemplate saveTemplate(Long userId, TaskTemplate template) {
        template.setUserId(userId);
        if (template.getDurationMinutes() == null) {
            template.setDurationMinutes(15);
        }
        if (template.getTimerTemplate() == null) {
            template.setTimerTemplate(0);
        }
        if (template.getId() == null) {
            taskTemplateMapper.insert(template);
        } else {
            TaskTemplate old = taskTemplateMapper.selectById(template.getId());
            if (old == null || old.getUserId() == null || !userId.equals(old.getUserId())) {
                throw new BizException(ErrorCode.FORBIDDEN);
            }
            taskTemplateMapper.updateById(template);
        }
        return template;
    }

    @Override
    public Task applyTemplate(Long userId, Long templateId) {
        TaskTemplate template = taskTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        Task task = new Task();
        task.setUserId(userId);
        task.setTemplateId(templateId);
        task.setTitle(template.getName());
        task.setContent(template.getContent());
        task.setCategory(template.getCategory());
        task.setDurationMinutes(template.getDurationMinutes());
        task.setSceneCode(template.getSceneCode());
        task.setSceneTags(template.getSceneTags());
        task.setPriority(1);
        task.setStatus(STATUS_TODO);
        task.setBuffered(0);
        task.setSourceType("TEMPLATE");
        taskMapper.insert(task);
        return task;
    }

    @Override
    public Task createTask(Long userId, Task task) {
        task.setId(null);
        task.setUserId(userId);
        if (task.getPriority() == null) {
            task.setPriority(1);
        }
        if (task.getDurationMinutes() == null) {
            task.setDurationMinutes(15);
        }
        if (task.getStatus() == null) {
            task.setStatus(STATUS_TODO);
        }
        if (task.getBuffered() == null) {
            task.setBuffered(0);
        }
        if (StringUtils.isBlank(task.getSourceType())) {
            task.setSourceType("MANUAL");
        }
        taskMapper.insert(task);
        return task;
    }

    @Override
    public List<Task> listTasks(Long userId, Integer status, String category) {
        return taskMapper.selectList(new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, userId)
                .eq(status != null, Task::getStatus, status)
                .eq(StringUtils.isNotBlank(category), Task::getCategory, category)
                .orderByAsc(Task::getPriority)
                .orderByDesc(Task::getId));
    }

    @Override
    public Task updateTask(Long userId, Task task) {
        Task old = ownedTask(userId, task.getId());
        task.setUserId(old.getUserId());
        taskMapper.updateById(task);
        return taskMapper.selectById(task.getId());
    }

    @Override
    public void buffer(Long userId, Long taskId) {
        Task task = ownedTask(userId, taskId);
        task.setBuffered(1);
        task.setStatus(STATUS_BUFFER);
        taskMapper.updateById(task);
    }

    @Override
    public void expireBatch(Long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        taskMapper.update(null, new LambdaUpdateWrapper<Task>()
                .eq(Task::getUserId, userId)
                .in(Task::getId, ids)
                .set(Task::getStatus, STATUS_EXPIRED));
    }

    @Override
    public List<Task> importTasks(Long userId, List<Task> tasks) {
        List<Task> created = new ArrayList<>();
        if (tasks == null) {
            return created;
        }
        for (Task task : tasks) {
            task.setSourceType("IMPORT");
            created.add(createTask(userId, task));
        }
        return created;
    }

    @Override
    public List<Task> match(Long userId, Integer idleMinutes, String sceneCode, String category, String energyStatus) {
        UserPreference pref = preference(userId);
        LocalDate today = LocalDate.now();
        if (matcher.isRestDay(pref, today)) {
            throw new BizException(ErrorCode.REST_DAY);
        }
        int idle = idleMinutes == null || idleMinutes < 1 ? 15 : idleMinutes;
        String energy = StringUtils.defaultIfBlank(energyStatus, pref == null ? "ENERGETIC" : pref.getEnergyStatus());
        List<Task> pool = matchable(userId, today);
        List<Task> result = matcher.match(pool, idle, sceneCode, category, energy, LocalTime.now(),
                today.getDayOfWeek() == DayOfWeek.SATURDAY || today.getDayOfWeek() == DayOfWeek.SUNDAY);
        redisUtils.set(RedisKey.taskToday(userId), String.valueOf(result.size()), 3600);
        if ("COMMUTE".equalsIgnoreCase(sceneCode)) {
            long commuteDone = taskMapper.selectCount(new LambdaQueryWrapper<Task>()
                    .eq(Task::getUserId, userId).eq(Task::getStatus, STATUS_DONE).eq(Task::getSceneCode, "COMMUTE"));
            if (commuteDone >= 20) {
                pointService.grantBadge(userId, "COMMUTE_MASTER");
            }
        }
        return result;
    }

    @Override
    public List<Task> preload(Long userId, Integer idleMinutes, String sceneCode) {
        UserPreference pref = preference(userId);
        if (matcher.inDnd(pref, LocalTime.now()) || matcher.isRestDay(pref, LocalDate.now())) {
            return List.of();
        }
        try {
            return match(userId, idleMinutes == null ? 30 : idleMinutes, sceneCode, null, null);
        } catch (BizException ex) {
            return List.of();
        }
    }

    @Override
    @Transactional
    public Task complete(Long userId, Long taskId, Integer startDelaySeconds) {
        Task task = ownedTask(userId, taskId);
        task.setStatus(STATUS_DONE);
        task.setStartDelaySeconds(startDelaySeconds);
        taskMapper.updateById(task);
        int minutes = task.getDurationMinutes() == null ? 0 : task.getDurationMinutes();
        pointService.grant(userId, minutes, "TASK", String.valueOf(taskId), task.getTitle());
        pointService.touchStreak(userId);
        addGoalMinutes(task.getGoalId(), minutes);
        maybeDelayBadge(userId);
        return task;
    }

    @Override
    @Transactional
    public Task skip(Long userId, Long taskId) {
        if (!pointService.consumeItem(userId, "SKIP_CARD")) {
            throw new BizException(ErrorCode.ITEM_NOT_ENOUGH);
        }
        Task task = ownedTask(userId, taskId);
        task.setStatus(STATUS_EXPIRED);
        taskMapper.updateById(task);
        return task;
    }

    @Override
    @Transactional
    public FocusSession startFocus(Long userId, FocusSession req) {
        String live = redisUtils.get(RedisKey.focusLive(userId));
        if (StringUtils.isNotBlank(live)) {
            throw new BizException(ErrorCode.FOCUS_INVALID, "已有进行中的计时");
        }
        long now = System.currentTimeMillis();
        FocusSession session = new FocusSession();
        session.setUserId(userId);
        session.setTaskId(req.getTaskId());
        session.setGoalId(req.getGoalId());
        session.setTitle(StringUtils.defaultIfBlank(req.getTitle(), "专注"));
        session.setCategory(req.getCategory());
        session.setClientStartTs(req.getClientStartTs() == null ? now : req.getClientStartTs());
        session.setServerStartTs(now);
        session.setPausedSeconds(0);
        session.setSourceType(StringUtils.defaultIfBlank(req.getSourceType(), "FOCUS"));
        session.setStatus(0);
        session.setPointGranted(0);
        focusSessionMapper.insert(session);
        redisUtils.set(RedisKey.focusLive(userId), String.valueOf(session.getId()), 12 * 3600);
        if (req.getTaskId() != null) {
            Task task = ownedTask(userId, req.getTaskId());
            task.setStatus(STATUS_DOING);
            taskMapper.updateById(task);
        }
        return session;
    }

    @Override
    public FocusSession pauseFocus(Long userId, Long sessionId) {
        FocusSession session = ownedSession(userId, sessionId);
        if (session.getStatus() != 0) {
            throw new BizException(ErrorCode.FOCUS_INVALID);
        }
        session.setStatus(1);
        session.setLastPauseTs(System.currentTimeMillis());
        focusSessionMapper.updateById(session);
        return session;
    }

    @Override
    public FocusSession resumeFocus(Long userId, Long sessionId) {
        FocusSession session = ownedSession(userId, sessionId);
        if (session.getStatus() != 1) {
            throw new BizException(ErrorCode.FOCUS_INVALID);
        }
        long extra = 0;
        if (session.getLastPauseTs() != null) {
            extra = Math.max(0, (System.currentTimeMillis() - session.getLastPauseTs()) / 1000);
        }
        session.setPausedSeconds((session.getPausedSeconds() == null ? 0 : session.getPausedSeconds()) + (int) extra);
        session.setLastPauseTs(null);
        session.setStatus(0);
        focusSessionMapper.updateById(session);
        return session;
    }

    @Override
    @Transactional
    public FocusSession finishFocus(Long userId, Long sessionId, Long clientEndTs, String remark) {
        FocusSession session = ownedSession(userId, sessionId);
        if (session.getStatus() != 0 && session.getStatus() != 1) {
            throw new BizException(ErrorCode.FOCUS_INVALID);
        }
        if (session.getStatus() == 1) {
            resumeFocus(userId, sessionId);
            session = ownedSession(userId, sessionId);
        }
        long serverEnd = System.currentTimeMillis();
        long clientEnd = clientEndTs == null ? serverEnd : clientEndTs;
        if (clientEnd < session.getClientStartTs()) {
            throw new BizException(ErrorCode.FOCUS_CHEAT);
        }
        int paused = session.getPausedSeconds() == null ? 0 : session.getPausedSeconds();
        int serverSec = (int) Math.max(0, (serverEnd - session.getServerStartTs()) / 1000 - paused);
        int clientSec = (int) Math.max(0, (clientEnd - session.getClientStartTs()) / 1000 - paused);
        if (clientSec > serverSec + 60) {
            clientSec = serverSec;
        }
        if (clientSec > 8 * 3600) {
            throw new BizException(ErrorCode.FOCUS_CHEAT);
        }
        session.setClientEndTs(clientEnd);
        session.setServerEndTs(serverEnd);
        session.setDurationSeconds(clientSec);
        session.setStatus(2);
        session.setRemark(remark);
        int points = clientSec >= 30 ? clientSec / 60 : 0;
        session.setPointGranted(points);
        focusSessionMapper.updateById(session);
        redisUtils.delete(RedisKey.focusLive(userId));
        if (points > 0) {
            pointService.grant(userId, points, "FOCUS", String.valueOf(session.getId()), session.getTitle());
            pointService.touchStreak(userId);
        }
        addGoalMinutes(session.getGoalId(), points);
        if (session.getTaskId() != null) {
            Task task = ownedTask(userId, session.getTaskId());
            task.setStatus(STATUS_DONE);
            taskMapper.updateById(task);
        }
        String week = weekKey();
        redisUtils.zIncr(RedisKey.weekRank(week), String.valueOf(userId), points);
        return session;
    }

    @Override
    public void abandonFocus(Long userId, Long sessionId) {
        FocusSession session = ownedSession(userId, sessionId);
        session.setStatus(3);
        session.setServerEndTs(System.currentTimeMillis());
        session.setDurationSeconds(0);
        session.setPointGranted(0);
        focusSessionMapper.updateById(session);
        redisUtils.delete(RedisKey.focusLive(userId));
        if (session.getTaskId() != null) {
            Task task = ownedTask(userId, session.getTaskId());
            task.setStatus(STATUS_TODO);
            taskMapper.updateById(task);
        }
    }

    @Override
    public FocusSession liveFocus(Long userId) {
        String id = redisUtils.get(RedisKey.focusLive(userId));
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return focusSessionMapper.selectById(Long.valueOf(id));
    }

    @Override
    public void deferUnfinished() {
        LocalDate today = LocalDate.now();
        taskMapper.update(null, new LambdaUpdateWrapper<Task>()
                .eq(Task::getStatus, STATUS_TODO)
                .eq(Task::getBuffered, 0)
                .lt(Task::getCreatedAt, today.atStartOfDay())
                .set(Task::getDeferredTo, today));
    }

    @Override
    public Map<String, Object> delayCurve(Long userId) {
        List<Task> done = taskMapper.selectList(new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, userId)
                .eq(Task::getStatus, STATUS_DONE)
                .isNotNull(Task::getStartDelaySeconds)
                .orderByAsc(Task::getId)
                .last("limit 30"));
        List<Integer> delays = done.stream().map(Task::getStartDelaySeconds).toList();
        Map<String, Object> map = new HashMap<>();
        map.put("points", delays);
        long total = taskMapper.selectCount(new LambdaQueryWrapper<Task>().eq(Task::getUserId, userId));
        long finished = taskMapper.selectCount(new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, userId).eq(Task::getStatus, STATUS_DONE));
        map.put("completionRate", total == 0 ? 0 : Math.round(finished * 1000.0 / total) / 10.0);
        if (delays.size() >= 5) {
            int first = delays.subList(0, Math.min(3, delays.size())).stream().mapToInt(i -> i).sum();
            int last = delays.subList(delays.size() - 3, delays.size()).stream().mapToInt(i -> i).sum();
            if (last < first) {
                pointService.grantBadge(userId, "DELAY_IMPROVER");
            }
        }
        return map;
    }

    private List<Task> matchable(Long userId, LocalDate today) {
        return taskMapper.selectList(new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, userId)
                .eq(Task::getStatus, STATUS_TODO)
                .and(w -> w.eq(Task::getBuffered, 0).or().isNull(Task::getBuffered))
                .and(w -> w.isNull(Task::getDeferredTo).or().le(Task::getDeferredTo, today)));
    }

    private Task ownedTask(Long userId, Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null || !userId.equals(task.getUserId())) {
            throw new BizException(ErrorCode.TASK_NOT_FOUND);
        }
        return task;
    }

    private FocusSession ownedSession(Long userId, Long sessionId) {
        FocusSession session = focusSessionMapper.selectById(sessionId);
        if (session == null || !userId.equals(session.getUserId())) {
            throw new BizException(ErrorCode.FOCUS_INVALID);
        }
        return session;
    }

    private UserPreference preference(Long userId) {
        return cache.get(CacheNames.PREFERENCE, String.valueOf(userId), UserPreference.class,
                () -> userPreferenceMapper.selectOne(new LambdaQueryWrapper<UserPreference>()
                        .eq(UserPreference::getUserId, userId)));
    }

    private void addGoalMinutes(Long goalId, int minutes) {
        if (goalId == null || minutes <= 0) {
            return;
        }
        UserGoal goal = userGoalMapper.selectById(goalId);
        if (goal == null) {
            return;
        }
        goal.setFinishedMinutes((goal.getFinishedMinutes() == null ? 0 : goal.getFinishedMinutes()) + minutes);
        if (goal.getTargetMinutes() != null && goal.getFinishedMinutes() >= goal.getTargetMinutes()) {
            goal.setStatus(1);
        }
        userGoalMapper.updateById(goal);
    }

    private void maybeDelayBadge(Long userId) {
        delayCurve(userId);
    }

    private String weekKey() {
        LocalDate now = LocalDate.now();
        LocalDate monday = now.with(DayOfWeek.MONDAY);
        return monday.toString();
    }
}
