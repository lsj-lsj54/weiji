package com.weiji.modules.point.service.impl; // 业务实现

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // MyBatis-Plus
import com.weiji.common.constant.CacheNames; // 本仓类 CacheNames
import com.weiji.common.enums.ErrorCode; // 本仓类 ErrorCode
import com.weiji.common.exception.BizException; // 本仓类 BizException
import com.weiji.common.utils.IdUtils; // 本仓类 IdUtils
import com.weiji.framework.cache.MultiLevelCache; // 本仓类 MultiLevelCache
import com.weiji.modules.point.entity.Badge; // 本仓类 Badge
import com.weiji.modules.point.entity.PointAccount; // 本仓类 PointAccount
import com.weiji.modules.point.entity.PointLedger; // 本仓类 PointLedger
import com.weiji.modules.point.entity.Reward; // 本仓类 Reward
import com.weiji.modules.point.entity.RewardRedemption; // 本仓类 RewardRedemption
import com.weiji.modules.point.entity.UserBadge; // 本仓类 UserBadge
import com.weiji.modules.point.entity.UserItem; // 本仓类 UserItem
import com.weiji.modules.point.mapper.BadgeMapper; // 本仓类 BadgeMapper
import com.weiji.modules.point.mapper.PointAccountMapper; // 本仓类 PointAccountMapper
import com.weiji.modules.point.mapper.PointLedgerMapper; // 本仓类 PointLedgerMapper
import com.weiji.modules.point.mapper.RewardMapper; // 本仓类 RewardMapper
import com.weiji.modules.point.mapper.RewardRedemptionMapper; // 本仓类 RewardRedemptionMapper
import com.weiji.modules.point.mapper.UserBadgeMapper; // 本仓类 UserBadgeMapper
import com.weiji.modules.point.mapper.UserItemMapper; // 本仓类 UserItemMapper
import com.weiji.modules.point.service.PointService; // 本仓类 PointService
import com.weiji.modules.task.entity.FocusSession; // 本仓类 FocusSession
import com.weiji.modules.task.entity.Task; // 本仓类 Task
import com.weiji.modules.task.mapper.FocusSessionMapper; // 本仓类 FocusSessionMapper
import com.weiji.modules.task.mapper.TaskMapper; // 本仓类 TaskMapper
import com.weiji.modules.user.entity.User; // 本仓类 User
import com.weiji.modules.user.entity.UserPreference; // 本仓类 UserPreference
import com.weiji.modules.user.mapper.UserMapper; // 本仓类 UserMapper
import com.weiji.modules.user.mapper.UserPreferenceMapper; // 本仓类 UserPreferenceMapper
import lombok.RequiredArgsConstructor; // Lombok 样板代码生成
import org.apache.commons.lang3.StringUtils; // 字符串工具
import org.springframework.stereotype.Service; // 业务层组件
import org.springframework.transaction.annotation.Transactional; // 事务

import java.time.DayOfWeek; // 日期时间
import java.time.LocalDate; // 日期时间
import java.time.LocalDateTime; // 日期时间
import java.util.ArrayList; // JDK 集合/工具
import java.util.HashMap; // JDK 集合/工具
import java.util.List; // JDK 集合/工具
import java.util.Map; // JDK 集合/工具
import java.util.Objects; // JDK 集合/工具

@Service // 业务层
@RequiredArgsConstructor // Lombok：为 final 字段生成构造器注入
public class PointServiceImpl implements PointService { // 定义类 PointServiceImpl，实现接口

    private final PointAccountMapper pointAccountMapper; // 构造注入 pointAccountMapper
    private final PointLedgerMapper pointLedgerMapper; // 构造注入 pointLedgerMapper
    private final RewardMapper rewardMapper; // 构造注入 rewardMapper
    private final RewardRedemptionMapper rewardRedemptionMapper; // 构造注入 rewardRedemptionMapper
    private final UserItemMapper userItemMapper; // 构造注入 userItemMapper
    private final BadgeMapper badgeMapper; // 构造注入 badgeMapper
    private final UserBadgeMapper userBadgeMapper; // 构造注入 userBadgeMapper
    private final UserMapper userMapper; // 构造注入 userMapper
    private final UserPreferenceMapper userPreferenceMapper; // 构造注入 userPreferenceMapper
    private final FocusSessionMapper focusSessionMapper; // 构造注入 focusSessionMapper
    private final TaskMapper taskMapper; // 构造注入 taskMapper
    private final MultiLevelCache cache; // 构造注入 多级缓存

    @Override // 覆盖父类/接口方法
    @Transactional // 声明式事务，异常则回滚
    public long grant(Long userId, long amount, String bizType, String bizId, String remark) { // 方法 grant
        if (amount <= 0) { // 条件判断
            return 0; // 返回结果
        }
        if (StringUtils.isNotBlank(bizId) && existsLedger(userId, bizType, bizId)) { // token 非空才解析
            return 0; // 返回结果
        }
        return apply(userId, amount, bizType, bizId, remark); // 返回结果
    }

    @Override // 覆盖父类/接口方法
    @Transactional // 声明式事务，异常则回滚
    public long spend(Long userId, long amount, String bizType, String bizId, String remark) { // 方法 spend
        if (amount <= 0) { // 条件判断
            return 0; // 返回结果
        }
        PointAccount account = lockAccount(userId); // 赋值或调用
        if (account.getBalance() < amount) { // 条件判断
            throw new BizException(ErrorCode.POINT_NOT_ENOUGH); // 抛出业务或运行时异常
        }
        return apply(userId, -amount, bizType, bizId, remark); // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public Map<String, Object> account(Long userId) { // 方法 account
        PointAccount account = lockAccount(userId); // 赋值或调用
        Map<String, Object> map = new HashMap<>(); // 赋值或调用
        map.put("balance", account.getBalance()); // 本行业务语句
        map.put("totalEarned", account.getTotalEarned()); // 本行业务语句
        User user = userMapper.selectById(userId); // 按主键查库
        map.put("streakDays", user == null ? 0 : user.getStreakDays()); // 赋值或调用
        return map; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public List<PointLedger> ledgers(Long userId, int limit) { // 方法 ledgers
        return pointLedgerMapper.selectList(new LambdaQueryWrapper<PointLedger>() // 查列表
                .eq(PointLedger::getUserId, userId) // 本行业务语句
                .orderByDesc(PointLedger::getId) // 本行业务语句
                .last("limit " + Math.min(Math.max(limit, 1), 100))); // 本行业务语句
    }

    @Override // 覆盖父类/接口方法
    public Reward createReward(Long userId, Reward reward) { // 方法 createReward
        reward.setId(null); // 本行业务语句
        reward.setUserId(userId); // 本行业务语句
        if (reward.getLockMode() == null) { // 条件判断
            reward.setLockMode(1); // 本行业务语句
        }
        rewardMapper.insert(reward); // 插入一行
        return reward; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public List<Reward> listRewards(Long userId) { // 方法 listRewards
        return rewardMapper.selectList(new LambdaQueryWrapper<Reward>().eq(Reward::getUserId, userId)); // 查列表
    }

    @Override // 覆盖父类/接口方法
    @Transactional // 声明式事务，异常则回滚
    public RewardRedemption redeem(Long userId, Long rewardId) { // 方法 redeem
        Reward reward = rewardMapper.selectById(rewardId); // 按主键查库
        if (reward == null || !userId.equals(reward.getUserId())) { // 条件判断
            throw new BizException(ErrorCode.NOT_FOUND); // 抛出业务或运行时异常
        }
        PointAccount account = lockAccount(userId); // 赋值或调用
        if (Objects.equals(reward.getLockMode(), 1) && account.getBalance() < reward.getPointCost()) { // 条件判断
            throw new BizException(ErrorCode.REWARD_LOCKED); // 抛出业务或运行时异常
        }
        if (reward.getCooldownHours() != null && reward.getCooldownHours() > 0) { // 条件判断
            RewardRedemption last = rewardRedemptionMapper.selectOne(new LambdaQueryWrapper<RewardRedemption>() // 查一条
                    .eq(RewardRedemption::getUserId, userId) // 本行业务语句
                    .eq(RewardRedemption::getRewardId, rewardId) // 本行业务语句
                    .orderByDesc(RewardRedemption::getId) // 本行业务语句
                    .last("limit 1")); // 本行业务语句
            if (last != null && last.getLockedUntil() != null && last.getLockedUntil().isAfter(LocalDateTime.now()) // 条件判断
                    && !"EXPIRED".equals(last.getStatus())) { // 开始代码块
                throw new BizException(ErrorCode.REWARD_LOCKED); // 抛出业务或运行时异常
            }
        }
        spend(userId, reward.getPointCost(), "REDEEM", "reward-" + rewardId + "-" + IdUtils.uuid(), reward.getName()); // 本行业务语句
        RewardRedemption redemption = new RewardRedemption(); // 赋值或调用
        redemption.setUserId(userId); // 本行业务语句
        redemption.setRewardId(rewardId); // 本行业务语句
        if (reward.getCooldownHours() != null && reward.getCooldownHours() > 0) { // 条件判断
            redemption.setStatus("LOCKED"); // 本行业务语句
            redemption.setLockedUntil(LocalDateTime.now().plusHours(reward.getCooldownHours())); // 本行业务语句
        } else { // 开始代码块
            redemption.setStatus("REDEEMED"); // 本行业务语句
        }
        rewardRedemptionMapper.insert(redemption); // 插入一行
        if (StringUtils.isNotBlank(reward.getItemCode()) && !"CUSTOM".equals(reward.getItemCode())) { // token 非空才解析
            addItem(userId, reward.getItemCode(), 1); // 本行业务语句
        }
        return redemption; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    @Transactional // 声明式事务，异常则回滚
    public void addItem(Long userId, String itemCode, int qty) { // 方法 addItem
        UserItem item = userItemMapper.selectOne(new LambdaQueryWrapper<UserItem>() // 查一条
                .eq(UserItem::getUserId, userId).eq(UserItem::getItemCode, itemCode)); // 本行业务语句
        if (item == null) { // 条件判断
            item = new UserItem(); // 赋值或调用
            item.setUserId(userId); // 本行业务语句
            item.setItemCode(itemCode); // 本行业务语句
            item.setQuantity(qty); // 本行业务语句
            userItemMapper.insert(item); // 插入一行
        } else { // 开始代码块
            item.setQuantity(item.getQuantity() + qty); // 本行业务语句
            userItemMapper.updateById(item); // 按主键更新
        }
    }

    @Override // 覆盖父类/接口方法
    @Transactional // 声明式事务，异常则回滚
    public boolean consumeItem(Long userId, String itemCode) { // 方法 consumeItem
        UserItem item = userItemMapper.selectOne(new LambdaQueryWrapper<UserItem>() // 查一条
                .eq(UserItem::getUserId, userId).eq(UserItem::getItemCode, itemCode)); // 本行业务语句
        if (item == null || item.getQuantity() == null || item.getQuantity() < 1) { // 条件判断
            return false; // 返回 false
        }
        item.setQuantity(item.getQuantity() - 1); // 本行业务语句
        userItemMapper.updateById(item); // 按主键更新
        return true; // 返回 true
    }

    @Override // 覆盖父类/接口方法
    public List<UserItem> items(Long userId) { // 方法 items
        return userItemMapper.selectList(new LambdaQueryWrapper<UserItem>().eq(UserItem::getUserId, userId)); // 查列表
    }

    @Override // 覆盖父类/接口方法
    @Transactional // 声明式事务，异常则回滚
    public void grantBadge(Long userId, String code) { // 方法 grantBadge
        Badge badge = badgeByCode(code); // 赋值或调用
        if (badge == null) { // 条件判断
            return; // 本行业务语句
        }
        long exists = userBadgeMapper.selectCount(new LambdaQueryWrapper<UserBadge>() // 计数
                .eq(UserBadge::getUserId, userId).eq(UserBadge::getBadgeId, badge.getId())); // 本行业务语句
        if (exists > 0) { // 条件判断
            return; // 本行业务语句
        }
        UserBadge ub = new UserBadge(); // 赋值或调用
        ub.setUserId(userId); // 本行业务语句
        ub.setBadgeId(badge.getId()); // 本行业务语句
        userBadgeMapper.insert(ub); // 插入一行
        grant(userId, 20, "MILESTONE", "badge-" + code, badge.getName()); // 本行业务语句
    }

    @Override // 覆盖父类/接口方法
    public List<Map<String, Object>> myBadges(Long userId) { // 方法 myBadges
        List<UserBadge> owned = userBadgeMapper.selectList(new LambdaQueryWrapper<UserBadge>() // 查列表
                .eq(UserBadge::getUserId, userId)); // 本行业务语句
        List<Map<String, Object>> list = new ArrayList<>(); // 赋值或调用
        for (UserBadge ub : owned) { // 循环
            Badge badge = badgeById(ub.getBadgeId()); // 赋值或调用
            Map<String, Object> row = new HashMap<>(); // 赋值或调用
            row.put("id", ub.getId()); // 本行业务语句
            row.put("badgeId", ub.getBadgeId()); // 本行业务语句
            row.put("code", badge == null ? null : badge.getCode()); // 赋值或调用
            row.put("name", badge == null ? ("徽章 " + ub.getBadgeId()) : badge.getName()); // 赋值或调用
            row.put("description", badge == null ? null : badge.getDescription()); // 赋值或调用
            list.add(row); // 本行业务语句
        }
        return list; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    @Transactional // 声明式事务，异常则回滚
    public void touchStreak(Long userId) { // 方法 touchStreak
        User user = userMapper.selectById(userId); // 按主键查库
        if (user == null) { // 条件判断
            return; // 本行业务语句
        }
        LocalDate today = LocalDate.now(); // 赋值或调用
        if (today.equals(user.getLastActiveDate())) { // 条件判断
            return; // 本行业务语句
        }
        UserPreference pref = cachedPreference(userId); // 赋值或调用
        boolean rest = isRestDay(pref, today); // 赋值或调用
        if (user.getLastActiveDate() == null) { // 条件判断
            user.setStreakDays(1); // 本行业务语句
        } else if (user.getLastActiveDate().plusDays(1).equals(today) || rest && user.getLastActiveDate().isBefore(today)) { // 开始代码块
            if (user.getLastActiveDate().plusDays(1).equals(today)) { // 条件判断
                user.setStreakDays((user.getStreakDays() == null ? 0 : user.getStreakDays()) + 1); // 赋值或调用
            }
        } else if (user.getLastActiveDate().isBefore(today.minusDays(1))) { // 开始代码块
            if (rest || consumeItem(userId, "DAY_EXEMPT")) { // 条件判断
                // 休息日或豁免权：不中断
            } else { // 开始代码块
                user.setStreakDays(1); // 本行业务语句
            }
        }
        user.setLastActiveDate(today); // 本行业务语句
        userMapper.updateById(user); // 按主键更新
        if (user.getStreakDays() != null && user.getStreakDays() >= 7) { // 条件判断
            grantBadge(userId, "STREAK_7"); // 本行业务语句
        }
    }

    @Override // 覆盖父类/接口方法
    public Map<String, Object> timeBill(Long userId, String range, LocalDate day) { // 方法 timeBill
        LocalDate[] span = span(range, day); // 赋值或调用
        List<FocusSession> sessions = focusSessionMapper.selectList(new LambdaQueryWrapper<FocusSession>() // 查列表
                .eq(FocusSession::getUserId, userId) // 本行业务语句
                .eq(FocusSession::getStatus, 2) // 本行业务语句
                .ge(FocusSession::getCreatedAt, span[0].atStartOfDay()) // 本行业务语句
                .lt(FocusSession::getCreatedAt, span[1].plusDays(1).atStartOfDay())); // 本行业务语句
        int focusSeconds = sessions.stream().mapToInt(s -> s.getDurationSeconds() == null ? 0 : s.getDurationSeconds()).sum(); // 赋值或调用
        int matchSeconds = sessions.stream().filter(s -> "MATCH".equals(s.getSourceType())) // 赋值或调用
                .mapToInt(s -> s.getDurationSeconds() == null ? 0 : s.getDurationSeconds()).sum(); // 赋值或调用
        int manualSeconds = focusSeconds - matchSeconds; // 赋值或调用
        Map<String, Integer> categoryMinutes = new HashMap<>(); // 赋值或调用
        for (FocusSession session : sessions) { // 循环
            String cat = StringUtils.defaultIfBlank(session.getCategory(), "其他"); // 赋值或调用
            categoryMinutes.merge(cat, (session.getDurationSeconds() == null ? 0 : session.getDurationSeconds()) / 60, Integer::sum); // 赋值或调用
        }
        Map<String, Object> map = new HashMap<>(); // 赋值或调用
        map.put("from", span[0]); // 本行业务语句
        map.put("to", span[1]); // 本行业务语句
        map.put("totalMinutes", focusSeconds / 60); // 本行业务语句
        map.put("manualMinutes", manualSeconds / 60); // 本行业务语句
        map.put("matchMinutes", matchSeconds / 60); // 本行业务语句
        map.put("categoryMinutes", categoryMinutes); // 本行业务语句
        map.put("eveningsEquivalent", Math.round((focusSeconds / 60.0) / 180.0 * 10) / 10.0); // 本行业务语句
        return map; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public Map<String, Object> focusBoard(Long userId, String range, LocalDate day) { // 方法 focusBoard
        Map<String, Object> bill = timeBill(userId, range, day); // 赋值或调用
        UserPreference pref = cachedPreference(userId); // 赋值或调用
        int target = 0; // 赋值或调用
        if (pref != null) { // 条件判断
            target = "week".equals(range) // 赋值或调用
                    ? (pref.getWeeklyFocusMinutes() == null ? 0 : pref.getWeeklyFocusMinutes()) // 赋值或调用
                    : (pref.getDailyFocusMinutes() == null ? 0 : pref.getDailyFocusMinutes()); // 赋值或调用
        }
        int done = (Integer) bill.get("totalMinutes"); // 赋值或调用
        bill.put("targetMinutes", target); // 本行业务语句
        bill.put("progress", target <= 0 ? 0 : Math.min(100, done * 100 / target)); // 赋值或调用
        if (target > 0 && done >= target) { // 条件判断
            grantBadge(userId, "FOCUS_DAILY"); // 本行业务语句
        }
        long taskCount = taskMapper.selectCount(new LambdaQueryWrapper<Task>() // 计数
                .eq(Task::getUserId, userId).eq(Task::getStatus, 2)); // 本行业务语句
        if (taskCount >= 100) { // 条件判断
            grantBadge(userId, "TASK_100"); // 本行业务语句
        }
        return bill; // 返回结果
    }

    private long apply(Long userId, long amount, String bizType, String bizId, String remark) { // 方法 apply
        PointAccount account = lockAccount(userId); // 赋值或调用
        PointLedger ledger = new PointLedger(); // 赋值或调用
        ledger.setUserId(userId); // 本行业务语句
        ledger.setLedgerNo(IdUtils.uuid()); // 本行业务语句
        ledger.setChangeAmount(amount); // 本行业务语句
        ledger.setBizType(bizType); // 本行业务语句
        ledger.setBizId(bizId); // 本行业务语句
        ledger.setRemark(remark); // 本行业务语句
        pointLedgerMapper.insert(ledger); // 插入一行
        account.setBalance(account.getBalance() + amount); // 本行业务语句
        if (amount > 0) { // 条件判断
            account.setTotalEarned(account.getTotalEarned() + amount); // 本行业务语句
        }
        pointAccountMapper.updateById(account); // 按主键更新
        return amount; // 返回结果
    }

    private boolean existsLedger(Long userId, String bizType, String bizId) { // 方法 existsLedger
        long cnt = pointLedgerMapper.selectCount(new LambdaQueryWrapper<PointLedger>() // 计数
                .eq(PointLedger::getUserId, userId) // 本行业务语句
                .eq(PointLedger::getBizType, bizType) // 本行业务语句
                .eq(PointLedger::getBizId, bizId)); // 本行业务语句
        return cnt > 0; // 返回结果
    }

    private PointAccount lockAccount(Long userId) { // 方法 lockAccount
        PointAccount account = pointAccountMapper.selectOne(new LambdaQueryWrapper<PointAccount>() // 查一条
                .eq(PointAccount::getUserId, userId) // 本行业务语句
                .last("for update")); // 本行业务语句
        if (account == null) { // 条件判断
            account = new PointAccount(); // 赋值或调用
            account.setUserId(userId); // 本行业务语句
            account.setBalance(0L); // 本行业务语句
            account.setTotalEarned(0L); // 本行业务语句
            pointAccountMapper.insert(account); // 插入一行
        }
        return account; // 返回结果
    }

    private List<Badge> allBadges() { // 方法 allBadges
        return cache.getList(CacheNames.BADGE, CacheNames.ALL, Badge.class, // 多级缓存读列表
                () -> badgeMapper.selectList(null)); // 查列表
    }

    private Badge badgeById(Long badgeId) { // 方法 badgeById
        if (badgeId == null) { // 条件判断
            return null; // 返回空
        }
        return allBadges().stream().filter(b -> badgeId.equals(b.getId())).findFirst().orElse(null); // 返回结果
    }

    private Badge badgeByCode(String code) { // 方法 badgeByCode
        if (StringUtils.isBlank(code)) { // 条件判断
            return null; // 返回空
        }
        return allBadges().stream().filter(b -> code.equals(b.getCode())).findFirst().orElse(null); // 返回结果
    }

    private UserPreference cachedPreference(Long userId) { // 方法 cachedPreference
        return cache.get(CacheNames.PREFERENCE, String.valueOf(userId), UserPreference.class, // 多级缓存读取（Caffeine→Redis→DB）
                () -> userPreferenceMapper.selectOne(new LambdaQueryWrapper<UserPreference>() // 查一条
                        .eq(UserPreference::getUserId, userId))); // 本行业务语句
    }

    private boolean isRestDay(UserPreference pref, LocalDate day) { // 方法 isRestDay
        if (pref == null || StringUtils.isBlank(pref.getRestDays())) { // 条件判断
            return false; // 返回 false
        }
        int mark = day.getDayOfWeek().getValue() % 7; // 赋值或调用
        return pref.getRestDays().contains(String.valueOf(mark)); // 返回结果
    }

    private LocalDate[] span(String range, LocalDate day) { // 方法 span
        LocalDate base = day == null ? LocalDate.now() : day; // 赋值或调用
        if ("week".equalsIgnoreCase(range)) { // 条件判断
            LocalDate from = base.with(DayOfWeek.MONDAY); // 赋值或调用
            return new LocalDate[]{from, from.plusDays(6)}; // 返回结果
        }
        if ("month".equalsIgnoreCase(range)) { // 条件判断
            LocalDate from = base.withDayOfMonth(1); // 赋值或调用
            return new LocalDate[]{from, from.plusMonths(1).minusDays(1)}; // 返回结果
        }
        return new LocalDate[]{base, base}; // 返回结果
    }
}
