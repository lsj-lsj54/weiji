package com.weiji.modules.task.service.impl; // 业务实现

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // MyBatis-Plus
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper; // MyBatis-Plus
import com.weiji.common.constant.CacheNames; // 本仓类 CacheNames
import com.weiji.common.constant.RedisKey; // 本仓类 RedisKey
import com.weiji.common.enums.ErrorCode; // 本仓类 ErrorCode
import com.weiji.common.exception.BizException; // 本仓类 BizException
import com.weiji.framework.cache.MultiLevelCache; // 本仓类 MultiLevelCache
import com.weiji.framework.redis.RedisUtils; // 本仓类 RedisUtils
import com.weiji.modules.point.service.PointService; // 本仓类 PointService
import com.weiji.modules.task.entity.FocusSession; // 本仓类 FocusSession
import com.weiji.modules.task.entity.Task; // 本仓类 Task
import com.weiji.modules.task.entity.TaskTemplate; // 本仓类 TaskTemplate
import com.weiji.modules.task.mapper.FocusSessionMapper; // 本仓类 FocusSessionMapper
import com.weiji.modules.task.mapper.TaskMapper; // 本仓类 TaskMapper
import com.weiji.modules.task.mapper.TaskTemplateMapper; // 本仓类 TaskTemplateMapper
import com.weiji.modules.task.service.TaskService; // 本仓类 TaskService
import com.weiji.modules.task.service.matcher.RuleTaskMatcher; // 本仓类 RuleTaskMatcher
import com.weiji.modules.user.entity.UserGoal; // 本仓类 UserGoal
import com.weiji.modules.user.entity.UserPreference; // 本仓类 UserPreference
import com.weiji.modules.user.mapper.UserGoalMapper; // 本仓类 UserGoalMapper
import com.weiji.modules.user.mapper.UserPreferenceMapper; // 本仓类 UserPreferenceMapper
import lombok.RequiredArgsConstructor; // Lombok 样板代码生成
import org.apache.commons.lang3.StringUtils; // 字符串工具
import org.springframework.stereotype.Service; // 业务层组件
import org.springframework.transaction.annotation.Transactional; // 事务

import java.time.DayOfWeek; // 日期时间
import java.time.LocalDate; // 日期时间
import java.time.LocalTime; // 日期时间
import java.util.ArrayList; // JDK 集合/工具
import java.util.HashMap; // JDK 集合/工具
import java.util.List; // JDK 集合/工具
import java.util.Map; // JDK 集合/工具

@Service // 业务层
@RequiredArgsConstructor // Lombok：为 final 字段生成构造器注入
public class TaskServiceImpl implements TaskService { // 定义类 TaskServiceImpl，实现接口

    private static final int STATUS_TODO = 0; // 字段 0
    private static final int STATUS_DOING = 1; // 字段 1
    private static final int STATUS_DONE = 2; // 字段 2
    private static final int STATUS_BUFFER = 3; // 字段 3
    private static final int STATUS_EXPIRED = 4; // 字段 4

    private final TaskMapper taskMapper; // 构造注入 taskMapper
    private final TaskTemplateMapper taskTemplateMapper; // 构造注入 taskTemplateMapper
    private final FocusSessionMapper focusSessionMapper; // 构造注入 focusSessionMapper
    private final UserPreferenceMapper userPreferenceMapper; // 构造注入 userPreferenceMapper
    private final UserGoalMapper userGoalMapper; // 构造注入 userGoalMapper
    private final RuleTaskMatcher matcher; // 构造注入 matcher
    private final PointService pointService; // 构造注入 pointService
    private final RedisUtils redisUtils; // 构造注入 Redis 封装
    private final MultiLevelCache cache; // 构造注入 多级缓存

    @Override // 覆盖父类/接口方法
    public List<TaskTemplate> templates(Long userId) { // 方法 templates
        List<TaskTemplate> system = cache.getList(CacheNames.SYS_TEMPLATE, CacheNames.ALL, TaskTemplate.class, // 多级缓存读列表
                () -> taskTemplateMapper.selectList(new LambdaQueryWrapper<TaskTemplate>() // 查列表
                        .isNull(TaskTemplate::getUserId) // 本行业务语句
                        .orderByAsc(TaskTemplate::getId))); // 本行业务语句
        List<TaskTemplate> mine = taskTemplateMapper.selectList(new LambdaQueryWrapper<TaskTemplate>() // 查列表
                .eq(TaskTemplate::getUserId, userId) // 本行业务语句
                .orderByAsc(TaskTemplate::getId)); // 本行业务语句
        List<TaskTemplate> result = new ArrayList<>(system.size() + mine.size()); // 赋值或调用
        result.addAll(system); // 本行业务语句
        result.addAll(mine); // 本行业务语句
        return result; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public TaskTemplate saveTemplate(Long userId, TaskTemplate template) { // 方法 saveTemplate
        template.setUserId(userId); // 本行业务语句
        if (template.getDurationMinutes() == null) { // 条件判断
            template.setDurationMinutes(15); // 本行业务语句
        }
        if (template.getTimerTemplate() == null) { // 条件判断
            template.setTimerTemplate(0); // 本行业务语句
        }
        if (template.getId() == null) { // 条件判断
            taskTemplateMapper.insert(template); // 插入一行
        } else { // 开始代码块
            TaskTemplate old = taskTemplateMapper.selectById(template.getId()); // 按主键查库
            if (old == null || old.getUserId() == null || !userId.equals(old.getUserId())) { // 条件判断
                throw new BizException(ErrorCode.FORBIDDEN); // 抛出业务或运行时异常
            }
            taskTemplateMapper.updateById(template); // 按主键更新
        }
        return template; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public Task applyTemplate(Long userId, Long templateId) { // 方法 applyTemplate
        TaskTemplate template = taskTemplateMapper.selectById(templateId); // 按主键查库
        if (template == null) { // 条件判断
            throw new BizException(ErrorCode.NOT_FOUND); // 抛出业务或运行时异常
        }
        Task task = new Task(); // 赋值或调用
        task.setUserId(userId); // 本行业务语句
        task.setTemplateId(templateId); // 本行业务语句
        task.setTitle(template.getName()); // 本行业务语句
        task.setContent(template.getContent()); // 本行业务语句
        task.setCategory(template.getCategory()); // 本行业务语句
        task.setDurationMinutes(template.getDurationMinutes()); // 本行业务语句
        task.setSceneCode(template.getSceneCode()); // 本行业务语句
        task.setSceneTags(template.getSceneTags()); // 本行业务语句
        task.setPriority(1); // 本行业务语句
        task.setStatus(STATUS_TODO); // 本行业务语句
        task.setBuffered(0); // 本行业务语句
        task.setSourceType("TEMPLATE"); // 本行业务语句
        taskMapper.insert(task); // 插入一行
        return task; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public Task createTask(Long userId, Task task) { // 方法 createTask
        task.setId(null); // 本行业务语句
        task.setUserId(userId); // 本行业务语句
        if (task.getPriority() == null) { // 条件判断
            task.setPriority(1); // 本行业务语句
        }
        if (task.getDurationMinutes() == null) { // 条件判断
            task.setDurationMinutes(15); // 本行业务语句
        }
        if (task.getStatus() == null) { // 条件判断
            task.setStatus(STATUS_TODO); // 本行业务语句
        }
        if (task.getBuffered() == null) { // 条件判断
            task.setBuffered(0); // 本行业务语句
        }
        if (StringUtils.isBlank(task.getSourceType())) { // 条件判断
            task.setSourceType("MANUAL"); // 本行业务语句
        }
        taskMapper.insert(task); // 插入一行
        return task; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public List<Task> listTasks(Long userId, Integer status, String category) { // 方法 listTasks
        return taskMapper.selectList(new LambdaQueryWrapper<Task>() // 查列表
                .eq(Task::getUserId, userId) // 本行业务语句
                .eq(status != null, Task::getStatus, status) // 赋值或调用
                .eq(StringUtils.isNotBlank(category), Task::getCategory, category) // token 非空才解析
                .orderByAsc(Task::getPriority) // 本行业务语句
                .orderByDesc(Task::getId)); // 本行业务语句
    }

    @Override // 覆盖父类/接口方法
    public Task updateTask(Long userId, Task task) { // 方法 updateTask
        Task old = ownedTask(userId, task.getId()); // 赋值或调用
        task.setUserId(old.getUserId()); // 本行业务语句
        taskMapper.updateById(task); // 按主键更新
        return taskMapper.selectById(task.getId()); // 按主键查库
    }

    @Override // 覆盖父类/接口方法
    public void buffer(Long userId, Long taskId) { // 方法 buffer
        Task task = ownedTask(userId, taskId); // 赋值或调用
        task.setBuffered(1); // 本行业务语句
        task.setStatus(STATUS_BUFFER); // 本行业务语句
        taskMapper.updateById(task); // 按主键更新
    }

    @Override // 覆盖父类/接口方法
    public void expireBatch(Long userId, List<Long> ids) { // 方法 expireBatch
        if (ids == null || ids.isEmpty()) { // 条件判断
            return; // 本行业务语句
        }
        taskMapper.update(null, new LambdaUpdateWrapper<Task>() // 类型安全更新
                .eq(Task::getUserId, userId) // 本行业务语句
                .in(Task::getId, ids) // 本行业务语句
                .set(Task::getStatus, STATUS_EXPIRED)); // 本行业务语句
    }

    @Override // 覆盖父类/接口方法
    public List<Task> importTasks(Long userId, List<Task> tasks) { // 方法 importTasks
        List<Task> created = new ArrayList<>(); // 赋值或调用
        if (tasks == null) { // 条件判断
            return created; // 返回结果
        }
        for (Task task : tasks) { // 循环
            task.setSourceType("IMPORT"); // 本行业务语句
            created.add(createTask(userId, task)); // 本行业务语句
        }
        return created; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public List<Task> match(Long userId, Integer idleMinutes, String sceneCode, String category, String energyStatus) { // 方法 match
        UserPreference pref = preference(userId); // 赋值或调用
        LocalDate today = LocalDate.now(); // 赋值或调用
        if (matcher.isRestDay(pref, today)) { // 条件判断
            throw new BizException(ErrorCode.REST_DAY); // 抛出业务或运行时异常
        }
        int idle = idleMinutes == null || idleMinutes < 1 ? 15 : idleMinutes; // 赋值或调用
        String energy = StringUtils.defaultIfBlank(energyStatus, pref == null ? "ENERGETIC" : pref.getEnergyStatus()); // 赋值或调用
        List<Task> pool = matchable(userId, today); // 赋值或调用
        List<Task> result = matcher.match(pool, idle, sceneCode, category, energy, LocalTime.now(), // 赋值或调用
                today.getDayOfWeek() == DayOfWeek.SATURDAY || today.getDayOfWeek() == DayOfWeek.SUNDAY); // 赋值或调用
        redisUtils.set(RedisKey.taskToday(userId), String.valueOf(result.size()), 3600); // 调用 Redis 封装
        if ("COMMUTE".equalsIgnoreCase(sceneCode)) { // 条件判断
            long commuteDone = taskMapper.selectCount(new LambdaQueryWrapper<Task>() // 计数
                    .eq(Task::getUserId, userId).eq(Task::getStatus, STATUS_DONE).eq(Task::getSceneCode, "COMMUTE")); // 本行业务语句
            if (commuteDone >= 20) { // 条件判断
                pointService.grantBadge(userId, "COMMUTE_MASTER"); // 本行业务语句
            }
        }
        return result; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public List<Task> preload(Long userId, Integer idleMinutes, String sceneCode) { // 方法 preload
        UserPreference pref = preference(userId); // 赋值或调用
        if (matcher.inDnd(pref, LocalTime.now()) || matcher.isRestDay(pref, LocalDate.now())) { // 条件判断
            return List.of(); // 返回结果
        }
        try { // 捕获异常
            return match(userId, idleMinutes == null ? 30 : idleMinutes, sceneCode, null, null); // 返回结果
        } catch (BizException ex) { // 开始代码块
            return List.of(); // 返回结果
        }
    }

    @Override // 覆盖父类/接口方法
    @Transactional // 声明式事务，异常则回滚
    public Task complete(Long userId, Long taskId, Integer startDelaySeconds) { // 方法 complete
        Task task = ownedTask(userId, taskId); // 赋值或调用
        task.setStatus(STATUS_DONE); // 本行业务语句
        task.setStartDelaySeconds(startDelaySeconds); // 本行业务语句
        taskMapper.updateById(task); // 按主键更新
        int minutes = task.getDurationMinutes() == null ? 0 : task.getDurationMinutes(); // 赋值或调用
        pointService.grant(userId, minutes, "TASK", String.valueOf(taskId), task.getTitle()); // 本行业务语句
        pointService.touchStreak(userId); // 本行业务语句
        addGoalMinutes(task.getGoalId(), minutes); // 本行业务语句
        maybeDelayBadge(userId); // 本行业务语句
        return task; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    @Transactional // 声明式事务，异常则回滚
    public Task skip(Long userId, Long taskId) { // 方法 skip
        if (!pointService.consumeItem(userId, "SKIP_CARD")) { // 条件判断
            throw new BizException(ErrorCode.ITEM_NOT_ENOUGH); // 抛出业务或运行时异常
        }
        Task task = ownedTask(userId, taskId); // 赋值或调用
        task.setStatus(STATUS_EXPIRED); // 本行业务语句
        taskMapper.updateById(task); // 按主键更新
        return task; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    @Transactional // 声明式事务，异常则回滚
    public FocusSession startFocus(Long userId, FocusSession req) { // 方法 startFocus
        String live = redisUtils.get(RedisKey.focusLive(userId)); // 调用 Redis 封装
        if (StringUtils.isNotBlank(live)) { // token 非空才解析
            throw new BizException(ErrorCode.FOCUS_INVALID, "已有进行中的计时"); // 抛出业务或运行时异常
        }
        long now = System.currentTimeMillis(); // 赋值或调用
        FocusSession session = new FocusSession(); // 赋值或调用
        session.setUserId(userId); // 本行业务语句
        session.setTaskId(req.getTaskId()); // 本行业务语句
        session.setGoalId(req.getGoalId()); // 本行业务语句
        session.setTitle(StringUtils.defaultIfBlank(req.getTitle(), "专注")); // 本行业务语句
        session.setCategory(req.getCategory()); // 本行业务语句
        session.setClientStartTs(req.getClientStartTs() == null ? now : req.getClientStartTs()); // 赋值或调用
        session.setServerStartTs(now); // 本行业务语句
        session.setPausedSeconds(0); // 本行业务语句
        session.setSourceType(StringUtils.defaultIfBlank(req.getSourceType(), "FOCUS")); // 本行业务语句
        session.setStatus(0); // 本行业务语句
        session.setPointGranted(0); // 本行业务语句
        focusSessionMapper.insert(session); // 插入一行
        redisUtils.set(RedisKey.focusLive(userId), String.valueOf(session.getId()), 12 * 3600); // 调用 Redis 封装
        if (req.getTaskId() != null) { // 条件判断
            Task task = ownedTask(userId, req.getTaskId()); // 赋值或调用
            task.setStatus(STATUS_DOING); // 本行业务语句
            taskMapper.updateById(task); // 按主键更新
        }
        return session; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public FocusSession pauseFocus(Long userId, Long sessionId) { // 方法 pauseFocus
        FocusSession session = ownedSession(userId, sessionId); // 赋值或调用
        if (session.getStatus() != 0) { // 条件判断
            throw new BizException(ErrorCode.FOCUS_INVALID); // 抛出业务或运行时异常
        }
        session.setStatus(1); // 本行业务语句
        session.setLastPauseTs(System.currentTimeMillis()); // 本行业务语句
        focusSessionMapper.updateById(session); // 按主键更新
        return session; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public FocusSession resumeFocus(Long userId, Long sessionId) { // 方法 resumeFocus
        FocusSession session = ownedSession(userId, sessionId); // 赋值或调用
        if (session.getStatus() != 1) { // 条件判断
            throw new BizException(ErrorCode.FOCUS_INVALID); // 抛出业务或运行时异常
        }
        long extra = 0; // 赋值或调用
        if (session.getLastPauseTs() != null) { // 条件判断
            extra = Math.max(0, (System.currentTimeMillis() - session.getLastPauseTs()) / 1000); // 赋值或调用
        }
        session.setPausedSeconds((session.getPausedSeconds() == null ? 0 : session.getPausedSeconds()) + (int) extra); // 赋值或调用
        session.setLastPauseTs(null); // 本行业务语句
        session.setStatus(0); // 本行业务语句
        focusSessionMapper.updateById(session); // 按主键更新
        return session; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    @Transactional // 声明式事务，异常则回滚
    public FocusSession finishFocus(Long userId, Long sessionId, Long clientEndTs, String remark) { // 方法 finishFocus
        FocusSession session = ownedSession(userId, sessionId); // 赋值或调用
        if (session.getStatus() != 0 && session.getStatus() != 1) { // 条件判断
            throw new BizException(ErrorCode.FOCUS_INVALID); // 抛出业务或运行时异常
        }
        if (session.getStatus() == 1) { // 条件判断
            resumeFocus(userId, sessionId); // 本行业务语句
            session = ownedSession(userId, sessionId); // 赋值或调用
        }
        long serverEnd = System.currentTimeMillis(); // 赋值或调用
        long clientEnd = clientEndTs == null ? serverEnd : clientEndTs; // 赋值或调用
        if (clientEnd < session.getClientStartTs()) { // 条件判断
            throw new BizException(ErrorCode.FOCUS_CHEAT); // 抛出业务或运行时异常
        }
        int paused = session.getPausedSeconds() == null ? 0 : session.getPausedSeconds(); // 赋值或调用
        int serverSec = (int) Math.max(0, (serverEnd - session.getServerStartTs()) / 1000 - paused); // 赋值或调用
        int clientSec = (int) Math.max(0, (clientEnd - session.getClientStartTs()) / 1000 - paused); // 赋值或调用
        if (clientSec > serverSec + 60) { // 条件判断
            clientSec = serverSec; // 赋值或调用
        }
        if (clientSec > 8 * 3600) { // 条件判断
            throw new BizException(ErrorCode.FOCUS_CHEAT); // 抛出业务或运行时异常
        }
        session.setClientEndTs(clientEnd); // 本行业务语句
        session.setServerEndTs(serverEnd); // 本行业务语句
        session.setDurationSeconds(clientSec); // 本行业务语句
        session.setStatus(2); // 本行业务语句
        session.setRemark(remark); // 本行业务语句
        int points = clientSec >= 30 ? clientSec / 60 : 0; // 赋值或调用
        session.setPointGranted(points); // 本行业务语句
        focusSessionMapper.updateById(session); // 按主键更新
        redisUtils.delete(RedisKey.focusLive(userId)); // 调用 Redis 封装
        if (points > 0) { // 条件判断
            pointService.grant(userId, points, "FOCUS", String.valueOf(session.getId()), session.getTitle()); // 本行业务语句
            pointService.touchStreak(userId); // 本行业务语句
        }
        addGoalMinutes(session.getGoalId(), points); // 本行业务语句
        if (session.getTaskId() != null) { // 条件判断
            Task task = ownedTask(userId, session.getTaskId()); // 赋值或调用
            task.setStatus(STATUS_DONE); // 本行业务语句
            taskMapper.updateById(task); // 按主键更新
        }
        String week = weekKey(); // 赋值或调用
        redisUtils.zIncr(RedisKey.weekRank(week), String.valueOf(userId), points); // 调用 Redis 封装
        return session; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public void abandonFocus(Long userId, Long sessionId) { // 方法 abandonFocus
        FocusSession session = ownedSession(userId, sessionId); // 赋值或调用
        session.setStatus(3); // 本行业务语句
        session.setServerEndTs(System.currentTimeMillis()); // 本行业务语句
        session.setDurationSeconds(0); // 本行业务语句
        session.setPointGranted(0); // 本行业务语句
        focusSessionMapper.updateById(session); // 按主键更新
        redisUtils.delete(RedisKey.focusLive(userId)); // 调用 Redis 封装
        if (session.getTaskId() != null) { // 条件判断
            Task task = ownedTask(userId, session.getTaskId()); // 赋值或调用
            task.setStatus(STATUS_TODO); // 本行业务语句
            taskMapper.updateById(task); // 按主键更新
        }
    }

    @Override // 覆盖父类/接口方法
    public FocusSession liveFocus(Long userId) { // 方法 liveFocus
        String id = redisUtils.get(RedisKey.focusLive(userId)); // 调用 Redis 封装
        if (StringUtils.isBlank(id)) { // 条件判断
            return null; // 返回空
        }
        return focusSessionMapper.selectById(Long.valueOf(id)); // 按主键查库
    }

    @Override // 覆盖父类/接口方法
    public void deferUnfinished() { // 方法 deferUnfinished
        LocalDate today = LocalDate.now(); // 赋值或调用
        taskMapper.update(null, new LambdaUpdateWrapper<Task>() // 类型安全更新
                .eq(Task::getStatus, STATUS_TODO) // 本行业务语句
                .eq(Task::getBuffered, 0) // 本行业务语句
                .lt(Task::getCreatedAt, today.atStartOfDay()) // 本行业务语句
                .set(Task::getDeferredTo, today)); // 本行业务语句
    }

    @Override // 覆盖父类/接口方法
    public Map<String, Object> delayCurve(Long userId) { // 方法 delayCurve
        List<Task> done = taskMapper.selectList(new LambdaQueryWrapper<Task>() // 查列表
                .eq(Task::getUserId, userId) // 本行业务语句
                .eq(Task::getStatus, STATUS_DONE) // 本行业务语句
                .isNotNull(Task::getStartDelaySeconds) // 本行业务语句
                .orderByAsc(Task::getId) // 本行业务语句
                .last("limit 30")); // 本行业务语句
        List<Integer> delays = done.stream().map(Task::getStartDelaySeconds).toList(); // 赋值或调用
        Map<String, Object> map = new HashMap<>(); // 赋值或调用
        map.put("points", delays); // 本行业务语句
        long total = taskMapper.selectCount(new LambdaQueryWrapper<Task>().eq(Task::getUserId, userId)); // 计数
        long finished = taskMapper.selectCount(new LambdaQueryWrapper<Task>() // 计数
                .eq(Task::getUserId, userId).eq(Task::getStatus, STATUS_DONE)); // 本行业务语句
        map.put("completionRate", total == 0 ? 0 : Math.round(finished * 1000.0 / total) / 10.0); // 赋值或调用
        if (delays.size() >= 5) { // 条件判断
            int first = delays.subList(0, Math.min(3, delays.size())).stream().mapToInt(i -> i).sum(); // 赋值或调用
            int last = delays.subList(delays.size() - 3, delays.size()).stream().mapToInt(i -> i).sum(); // 赋值或调用
            if (last < first) { // 条件判断
                pointService.grantBadge(userId, "DELAY_IMPROVER"); // 本行业务语句
            }
        }
        return map; // 返回结果
    }

    private List<Task> matchable(Long userId, LocalDate today) { // 方法 matchable
        return taskMapper.selectList(new LambdaQueryWrapper<Task>() // 查列表
                .eq(Task::getUserId, userId) // 本行业务语句
                .eq(Task::getStatus, STATUS_TODO) // 本行业务语句
                .and(w -> w.eq(Task::getBuffered, 0).or().isNull(Task::getBuffered)) // 本行业务语句
                .and(w -> w.isNull(Task::getDeferredTo).or().le(Task::getDeferredTo, today))); // 本行业务语句
    }

    private Task ownedTask(Long userId, Long taskId) { // 方法 ownedTask
        Task task = taskMapper.selectById(taskId); // 按主键查库
        if (task == null || !userId.equals(task.getUserId())) { // 条件判断
            throw new BizException(ErrorCode.TASK_NOT_FOUND); // 抛出业务或运行时异常
        }
        return task; // 返回结果
    }

    private FocusSession ownedSession(Long userId, Long sessionId) { // 方法 ownedSession
        FocusSession session = focusSessionMapper.selectById(sessionId); // 按主键查库
        if (session == null || !userId.equals(session.getUserId())) { // 条件判断
            throw new BizException(ErrorCode.FOCUS_INVALID); // 抛出业务或运行时异常
        }
        return session; // 返回结果
    }

    private UserPreference preference(Long userId) { // 方法 preference
        return cache.get(CacheNames.PREFERENCE, String.valueOf(userId), UserPreference.class, // 多级缓存读取（Caffeine→Redis→DB）
                () -> userPreferenceMapper.selectOne(new LambdaQueryWrapper<UserPreference>() // 查一条
                        .eq(UserPreference::getUserId, userId))); // 本行业务语句
    }

    private void addGoalMinutes(Long goalId, int minutes) { // 方法 addGoalMinutes
        if (goalId == null || minutes <= 0) { // 条件判断
            return; // 本行业务语句
        }
        UserGoal goal = userGoalMapper.selectById(goalId); // 按主键查库
        if (goal == null) { // 条件判断
            return; // 本行业务语句
        }
        goal.setFinishedMinutes((goal.getFinishedMinutes() == null ? 0 : goal.getFinishedMinutes()) + minutes); // 赋值或调用
        if (goal.getTargetMinutes() != null && goal.getFinishedMinutes() >= goal.getTargetMinutes()) { // 条件判断
            goal.setStatus(1); // 本行业务语句
        }
        userGoalMapper.updateById(goal); // 按主键更新
    }

    private void maybeDelayBadge(Long userId) { // 方法 maybeDelayBadge
        delayCurve(userId); // 本行业务语句
    }

    private String weekKey() { // 方法 weekKey
        LocalDate now = LocalDate.now(); // 赋值或调用
        LocalDate monday = now.with(DayOfWeek.MONDAY); // 赋值或调用
        return monday.toString(); // 返回结果
    }
}
