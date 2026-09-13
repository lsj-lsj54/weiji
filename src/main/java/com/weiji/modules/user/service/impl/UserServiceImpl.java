package com.weiji.modules.user.service.impl; // 业务实现

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // MyBatis-Plus
import com.weiji.common.constant.CacheNames; // 本仓类 CacheNames
import com.weiji.common.enums.ErrorCode; // 本仓类 ErrorCode
import com.weiji.common.exception.BizException; // 本仓类 BizException
import com.weiji.framework.cache.MultiLevelCache; // 本仓类 MultiLevelCache
import com.weiji.framework.security.Currents; // 本仓类 Currents
import com.weiji.modules.task.entity.Task; // 本仓类 Task
import com.weiji.modules.task.service.TaskService; // 本仓类 TaskService
import com.weiji.modules.user.dto.UpdateProfileRequest; // 本仓类 UpdateProfileRequest
import com.weiji.modules.user.entity.User; // 本仓类 User
import com.weiji.modules.user.entity.UserGoal; // 本仓类 UserGoal
import com.weiji.modules.user.entity.UserPreference; // 本仓类 UserPreference
import com.weiji.modules.user.mapper.UserGoalMapper; // 本仓类 UserGoalMapper
import com.weiji.modules.user.mapper.UserMapper; // 本仓类 UserMapper
import com.weiji.modules.user.mapper.UserPreferenceMapper; // 本仓类 UserPreferenceMapper
import com.weiji.modules.user.service.UserService; // 本仓类 UserService
import com.weiji.modules.user.vo.UserVO; // 本仓类 UserVO
import lombok.RequiredArgsConstructor; // Lombok 样板代码生成
import org.apache.commons.lang3.StringUtils; // 字符串工具
import org.springframework.stereotype.Service; // 业务层组件
import org.springframework.transaction.annotation.Transactional; // 事务

import java.util.ArrayList; // JDK 集合/工具
import java.util.List; // JDK 集合/工具

@Service // 业务层
@RequiredArgsConstructor // Lombok：为 final 字段生成构造器注入
public class UserServiceImpl implements UserService { // 定义类 UserServiceImpl，实现接口

    private final UserMapper userMapper; // 构造注入 userMapper
    private final UserPreferenceMapper userPreferenceMapper; // 构造注入 userPreferenceMapper
    private final UserGoalMapper userGoalMapper; // 构造注入 userGoalMapper
    private final TaskService taskService; // 构造注入 taskService
    private final MultiLevelCache cache; // 构造注入 多级缓存

    @Override // 覆盖父类/接口方法
    public UserVO currentUser() { // 方法 currentUser
        UserVO vo = getUser(Currents.userId()); // 取当前登录用户 ID
        if (vo == null) { // 条件判断
            throw new BizException(ErrorCode.USER_NOT_FOUND); // 抛出业务或运行时异常
        }
        return vo; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public UserVO getUser(Long userId) { // 方法 getUser
        if (userId == null) { // 条件判断
            return null; // 返回空
        }
        return cache.get(CacheNames.USER, String.valueOf(userId), UserVO.class, () -> { // 多级缓存读取（Caffeine→Redis→DB）
            User user = userMapper.selectById(userId); // 按主键查库
            return user == null ? null : toVo(user); // 返回结果
        });
    }

    @Override // 覆盖父类/接口方法
    public UserVO updateProfile(UpdateProfileRequest request) { // 方法 updateProfile
        User user = loadCurrent(); // 赋值或调用
        if (StringUtils.isNotBlank(request.getNickname())) { // token 非空才解析
            user.setNickname(request.getNickname()); // 本行业务语句
        }
        if (StringUtils.isNotBlank(request.getAvatar())) { // token 非空才解析
            user.setAvatar(request.getAvatar()); // 本行业务语句
        }
        userMapper.updateById(user); // 按主键更新
        cache.evict(CacheNames.USER, String.valueOf(user.getId())); // 写后删 L1/L2 并广播失效
        UserVO vo = getUser(user.getId()); // 赋值或调用
        if (vo == null) { // 条件判断
            throw new BizException(ErrorCode.USER_NOT_FOUND); // 抛出业务或运行时异常
        }
        return vo; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public UserPreference preference() { // 方法 preference
        Long userId = Currents.userId(); // 取当前登录用户 ID
        UserPreference pref = loadPreference(userId); // 赋值或调用
        if (pref == null) { // 条件判断
            pref = new UserPreference(); // 赋值或调用
            pref.setUserId(userId); // 本行业务语句
            pref.setEnergyStatus("ENERGETIC"); // 本行业务语句
            userPreferenceMapper.insert(pref); // 插入一行
            cache.evict(CacheNames.PREFERENCE, String.valueOf(userId)); // 写后删 L1/L2 并广播失效
        }
        return pref; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public UserPreference updatePreference(UserPreference preference) { // 方法 updatePreference
        UserPreference old = preference(); // 赋值或调用
        preference.setId(old.getId()); // 本行业务语句
        preference.setUserId(old.getUserId()); // 本行业务语句
        userPreferenceMapper.updateById(preference); // 按主键更新
        cache.evict(CacheNames.PREFERENCE, String.valueOf(old.getUserId())); // 写后删 L1/L2 并广播失效
        return loadPreference(old.getUserId()); // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public UserGoal createGoal(UserGoal goal) { // 方法 createGoal
        goal.setId(null); // 本行业务语句
        goal.setUserId(Currents.userId()); // 取当前登录用户 ID
        if (goal.getStatus() == null) { // 条件判断
            goal.setStatus(0); // 本行业务语句
        }
        if (goal.getFinishedMinutes() == null) { // 条件判断
            goal.setFinishedMinutes(0); // 本行业务语句
        }
        userGoalMapper.insert(goal); // 插入一行
        return goal; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public List<UserGoal> goals() { // 方法 goals
        return userGoalMapper.selectList(new LambdaQueryWrapper<UserGoal>() // 查列表
                .eq(UserGoal::getUserId, Currents.userId()) // 取当前登录用户 ID
                .orderByDesc(UserGoal::getId)); // 本行业务语句
    }

    @Override // 覆盖父类/接口方法
    @Transactional // 声明式事务，异常则回滚
    public List<Task> decomposeGoal(Long goalId, Integer chunkMinutes) { // 方法 decomposeGoal
        Long userId = Currents.userId(); // 取当前登录用户 ID
        UserGoal goal = userGoalMapper.selectById(goalId); // 按主键查库
        if (goal == null || !userId.equals(goal.getUserId())) { // 条件判断
            throw new BizException(ErrorCode.NOT_FOUND); // 抛出业务或运行时异常
        }
        int chunk = chunkMinutes == null ? 15 : Math.min(30, Math.max(5, chunkMinutes)); // 赋值或调用
        int total = goal.getTargetMinutes() == null || goal.getTargetMinutes() < chunk ? chunk : goal.getTargetMinutes(); // 赋值或调用
        List<Task> created = new ArrayList<>(); // 赋值或调用
        int order = 1; // 赋值或调用
        int remain = total; // 赋值或调用
        while (remain > 0) { // 循环
            int minutes = Math.min(chunk, remain); // 赋值或调用
            Task task = new Task(); // 赋值或调用
            task.setTitle(goal.getTitle() + " · 第" + order + "段"); // 本行业务语句
            task.setContent(goal.getDescription()); // 本行业务语句
            task.setGoalId(goalId); // 本行业务语句
            task.setDurationMinutes(minutes); // 本行业务语句
            task.setPriority(1); // 本行业务语句
            task.setCategory("自我提升"); // 本行业务语句
            task.setSourceType("GOAL"); // 本行业务语句
            task.setSortOrder(order); // 本行业务语句
            created.add(taskService.createTask(userId, task)); // 本行业务语句
            remain -= minutes; // 赋值或调用
            order++; // 本行业务语句
        }
        return created; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public UserVO searchByPhone(String phone) { // 方法 searchByPhone
        if (StringUtils.isBlank(phone)) { // 条件判断
            throw new BizException(ErrorCode.BAD_REQUEST, "手机号不能为空"); // 抛出业务或运行时异常
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone.trim())); // 查一条
        if (user == null) { // 条件判断
            throw new BizException(ErrorCode.USER_NOT_FOUND); // 抛出业务或运行时异常
        }
        return toVo(user); // 返回结果
    }

    private UserPreference loadPreference(Long userId) { // 方法 loadPreference
        return cache.get(CacheNames.PREFERENCE, String.valueOf(userId), UserPreference.class, // 多级缓存读取（Caffeine→Redis→DB）
                () -> userPreferenceMapper.selectOne(new LambdaQueryWrapper<UserPreference>() // 查一条
                        .eq(UserPreference::getUserId, userId))); // 本行业务语句
    }

    private User loadCurrent() { // 方法 loadCurrent
        User user = userMapper.selectById(Currents.userId()); // 按主键查库
        if (user == null) { // 条件判断
            throw new BizException(ErrorCode.USER_NOT_FOUND); // 抛出业务或运行时异常
        }
        return user; // 返回结果
    }

    private UserVO toVo(User user) { // 方法 toVo
        return UserVO.builder() // 返回结果
                .id(user.getId()) // 本行业务语句
                .phone(user.getPhone()) // 本行业务语句
                .nickname(user.getNickname()) // 本行业务语句
                .avatar(user.getAvatar()) // 本行业务语句
                .status(user.getStatus()) // 本行业务语句
                .build(); // 本行业务语句
    }
}
