package com.weiji.modules.point.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.weiji.common.enums.ErrorCode;
import com.weiji.common.exception.BizException;
import com.weiji.common.utils.IdUtils;
import com.weiji.modules.point.entity.Badge;
import com.weiji.modules.point.entity.PointAccount;
import com.weiji.modules.point.entity.PointLedger;
import com.weiji.modules.point.entity.Reward;
import com.weiji.modules.point.entity.RewardRedemption;
import com.weiji.modules.point.entity.UserBadge;
import com.weiji.modules.point.entity.UserItem;
import com.weiji.modules.point.mapper.BadgeMapper;
import com.weiji.modules.point.mapper.PointAccountMapper;
import com.weiji.modules.point.mapper.PointLedgerMapper;
import com.weiji.modules.point.mapper.RewardMapper;
import com.weiji.modules.point.mapper.RewardRedemptionMapper;
import com.weiji.modules.point.mapper.UserBadgeMapper;
import com.weiji.modules.point.mapper.UserItemMapper;
import com.weiji.modules.point.service.PointService;
import com.weiji.modules.task.entity.FocusSession;
import com.weiji.modules.task.entity.Task;
import com.weiji.modules.task.mapper.FocusSessionMapper;
import com.weiji.modules.task.mapper.TaskMapper;
import com.weiji.modules.user.entity.User;
import com.weiji.modules.user.entity.UserPreference;
import com.weiji.modules.user.mapper.UserMapper;
import com.weiji.modules.user.mapper.UserPreferenceMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final PointAccountMapper pointAccountMapper;
    private final PointLedgerMapper pointLedgerMapper;
    private final RewardMapper rewardMapper;
    private final RewardRedemptionMapper rewardRedemptionMapper;
    private final UserItemMapper userItemMapper;
    private final BadgeMapper badgeMapper;
    private final UserBadgeMapper userBadgeMapper;
    private final UserMapper userMapper;
    private final UserPreferenceMapper userPreferenceMapper;
    private final FocusSessionMapper focusSessionMapper;
    private final TaskMapper taskMapper;

    @Override
    @Transactional
    public long grant(Long userId, long amount, String bizType, String bizId, String remark) {
        if (amount <= 0) {
            return 0;
        }
        if (StringUtils.isNotBlank(bizId) && existsLedger(userId, bizType, bizId)) {
            return 0;
        }
        return apply(userId, amount, bizType, bizId, remark);
    }

    @Override
    @Transactional
    public long spend(Long userId, long amount, String bizType, String bizId, String remark) {
        if (amount <= 0) {
            return 0;
        }
        PointAccount account = lockAccount(userId);
        if (account.getBalance() < amount) {
            throw new BizException(ErrorCode.POINT_NOT_ENOUGH);
        }
        return apply(userId, -amount, bizType, bizId, remark);
    }

    @Override
    public Map<String, Object> account(Long userId) {
        PointAccount account = lockAccount(userId);
        Map<String, Object> map = new HashMap<>();
        map.put("balance", account.getBalance());
        map.put("totalEarned", account.getTotalEarned());
        User user = userMapper.selectById(userId);
        map.put("streakDays", user == null ? 0 : user.getStreakDays());
        return map;
    }

    @Override
    public List<PointLedger> ledgers(Long userId, int limit) {
        return pointLedgerMapper.selectList(new LambdaQueryWrapper<PointLedger>()
                .eq(PointLedger::getUserId, userId)
                .orderByDesc(PointLedger::getId)
                .last("limit " + Math.min(Math.max(limit, 1), 100)));
    }

    @Override
    public Reward createReward(Long userId, Reward reward) {
        reward.setId(null);
        reward.setUserId(userId);
        if (reward.getLockMode() == null) {
            reward.setLockMode(1);
        }
        rewardMapper.insert(reward);
        return reward;
    }

    @Override
    public List<Reward> listRewards(Long userId) {
        return rewardMapper.selectList(new LambdaQueryWrapper<Reward>().eq(Reward::getUserId, userId));
    }

    @Override
    @Transactional
    public RewardRedemption redeem(Long userId, Long rewardId) {
        Reward reward = rewardMapper.selectById(rewardId);
        if (reward == null || !userId.equals(reward.getUserId())) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        PointAccount account = lockAccount(userId);
        if (Objects.equals(reward.getLockMode(), 1) && account.getBalance() < reward.getPointCost()) {
            throw new BizException(ErrorCode.REWARD_LOCKED);
        }
        if (reward.getCooldownHours() != null && reward.getCooldownHours() > 0) {
            RewardRedemption last = rewardRedemptionMapper.selectOne(new LambdaQueryWrapper<RewardRedemption>()
                    .eq(RewardRedemption::getUserId, userId)
                    .eq(RewardRedemption::getRewardId, rewardId)
                    .orderByDesc(RewardRedemption::getId)
                    .last("limit 1"));
            if (last != null && last.getLockedUntil() != null && last.getLockedUntil().isAfter(LocalDateTime.now())
                    && !"EXPIRED".equals(last.getStatus())) {
                throw new BizException(ErrorCode.REWARD_LOCKED);
            }
        }
        spend(userId, reward.getPointCost(), "REDEEM", "reward-" + rewardId + "-" + IdUtils.uuid(), reward.getName());
        RewardRedemption redemption = new RewardRedemption();
        redemption.setUserId(userId);
        redemption.setRewardId(rewardId);
        if (reward.getCooldownHours() != null && reward.getCooldownHours() > 0) {
            redemption.setStatus("LOCKED");
            redemption.setLockedUntil(LocalDateTime.now().plusHours(reward.getCooldownHours()));
        } else {
            redemption.setStatus("REDEEMED");
        }
        rewardRedemptionMapper.insert(redemption);
        if (StringUtils.isNotBlank(reward.getItemCode()) && !"CUSTOM".equals(reward.getItemCode())) {
            addItem(userId, reward.getItemCode(), 1);
        }
        return redemption;
    }

    @Override
    @Transactional
    public void addItem(Long userId, String itemCode, int qty) {
        UserItem item = userItemMapper.selectOne(new LambdaQueryWrapper<UserItem>()
                .eq(UserItem::getUserId, userId).eq(UserItem::getItemCode, itemCode));
        if (item == null) {
            item = new UserItem();
            item.setUserId(userId);
            item.setItemCode(itemCode);
            item.setQuantity(qty);
            userItemMapper.insert(item);
        } else {
            item.setQuantity(item.getQuantity() + qty);
            userItemMapper.updateById(item);
        }
    }

    @Override
    @Transactional
    public boolean consumeItem(Long userId, String itemCode) {
        UserItem item = userItemMapper.selectOne(new LambdaQueryWrapper<UserItem>()
                .eq(UserItem::getUserId, userId).eq(UserItem::getItemCode, itemCode));
        if (item == null || item.getQuantity() == null || item.getQuantity() < 1) {
            return false;
        }
        item.setQuantity(item.getQuantity() - 1);
        userItemMapper.updateById(item);
        return true;
    }

    @Override
    public List<UserItem> items(Long userId) {
        return userItemMapper.selectList(new LambdaQueryWrapper<UserItem>().eq(UserItem::getUserId, userId));
    }

    @Override
    @Transactional
    public void grantBadge(Long userId, String code) {
        Badge badge = badgeMapper.selectOne(new LambdaQueryWrapper<Badge>().eq(Badge::getCode, code));
        if (badge == null) {
            return;
        }
        long exists = userBadgeMapper.selectCount(new LambdaQueryWrapper<UserBadge>()
                .eq(UserBadge::getUserId, userId).eq(UserBadge::getBadgeId, badge.getId()));
        if (exists > 0) {
            return;
        }
        UserBadge ub = new UserBadge();
        ub.setUserId(userId);
        ub.setBadgeId(badge.getId());
        userBadgeMapper.insert(ub);
        grant(userId, 20, "MILESTONE", "badge-" + code, badge.getName());
    }

    @Override
    public List<Map<String, Object>> myBadges(Long userId) {
        List<UserBadge> owned = userBadgeMapper.selectList(new LambdaQueryWrapper<UserBadge>()
                .eq(UserBadge::getUserId, userId));
        List<Map<String, Object>> list = new ArrayList<>();
        for (UserBadge ub : owned) {
            Badge badge = badgeMapper.selectById(ub.getBadgeId());
            Map<String, Object> row = new HashMap<>();
            row.put("id", ub.getId());
            row.put("badgeId", ub.getBadgeId());
            row.put("code", badge == null ? null : badge.getCode());
            row.put("name", badge == null ? ("徽章 " + ub.getBadgeId()) : badge.getName());
            row.put("description", badge == null ? null : badge.getDescription());
            list.add(row);
        }
        return list;
    }

    @Override
    @Transactional
    public void touchStreak(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }
        LocalDate today = LocalDate.now();
        if (today.equals(user.getLastActiveDate())) {
            return;
        }
        UserPreference pref = userPreferenceMapper.selectOne(new LambdaQueryWrapper<UserPreference>()
                .eq(UserPreference::getUserId, userId));
        boolean rest = isRestDay(pref, today);
        if (user.getLastActiveDate() == null) {
            user.setStreakDays(1);
        } else if (user.getLastActiveDate().plusDays(1).equals(today) || rest && user.getLastActiveDate().isBefore(today)) {
            if (user.getLastActiveDate().plusDays(1).equals(today)) {
                user.setStreakDays((user.getStreakDays() == null ? 0 : user.getStreakDays()) + 1);
            }
        } else if (user.getLastActiveDate().isBefore(today.minusDays(1))) {
            if (rest || consumeItem(userId, "DAY_EXEMPT")) {
                // 休息日或豁免权：不中断
            } else {
                user.setStreakDays(1);
            }
        }
        user.setLastActiveDate(today);
        userMapper.updateById(user);
        if (user.getStreakDays() != null && user.getStreakDays() >= 7) {
            grantBadge(userId, "STREAK_7");
        }
    }

    @Override
    public Map<String, Object> timeBill(Long userId, String range, LocalDate day) {
        LocalDate[] span = span(range, day);
        List<FocusSession> sessions = focusSessionMapper.selectList(new LambdaQueryWrapper<FocusSession>()
                .eq(FocusSession::getUserId, userId)
                .eq(FocusSession::getStatus, 2)
                .ge(FocusSession::getCreatedAt, span[0].atStartOfDay())
                .lt(FocusSession::getCreatedAt, span[1].plusDays(1).atStartOfDay()));
        int focusSeconds = sessions.stream().mapToInt(s -> s.getDurationSeconds() == null ? 0 : s.getDurationSeconds()).sum();
        int matchSeconds = sessions.stream().filter(s -> "MATCH".equals(s.getSourceType()))
                .mapToInt(s -> s.getDurationSeconds() == null ? 0 : s.getDurationSeconds()).sum();
        int manualSeconds = focusSeconds - matchSeconds;
        Map<String, Integer> categoryMinutes = new HashMap<>();
        for (FocusSession session : sessions) {
            String cat = StringUtils.defaultIfBlank(session.getCategory(), "其他");
            categoryMinutes.merge(cat, (session.getDurationSeconds() == null ? 0 : session.getDurationSeconds()) / 60, Integer::sum);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("from", span[0]);
        map.put("to", span[1]);
        map.put("totalMinutes", focusSeconds / 60);
        map.put("manualMinutes", manualSeconds / 60);
        map.put("matchMinutes", matchSeconds / 60);
        map.put("categoryMinutes", categoryMinutes);
        map.put("eveningsEquivalent", Math.round((focusSeconds / 60.0) / 180.0 * 10) / 10.0);
        return map;
    }

    @Override
    public Map<String, Object> focusBoard(Long userId, String range, LocalDate day) {
        Map<String, Object> bill = timeBill(userId, range, day);
        UserPreference pref = userPreferenceMapper.selectOne(new LambdaQueryWrapper<UserPreference>()
                .eq(UserPreference::getUserId, userId));
        int target = 0;
        if (pref != null) {
            target = "week".equals(range)
                    ? (pref.getWeeklyFocusMinutes() == null ? 0 : pref.getWeeklyFocusMinutes())
                    : (pref.getDailyFocusMinutes() == null ? 0 : pref.getDailyFocusMinutes());
        }
        int done = (Integer) bill.get("totalMinutes");
        bill.put("targetMinutes", target);
        bill.put("progress", target <= 0 ? 0 : Math.min(100, done * 100 / target));
        if (target > 0 && done >= target) {
            grantBadge(userId, "FOCUS_DAILY");
        }
        long taskCount = taskMapper.selectCount(new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, userId).eq(Task::getStatus, 2));
        if (taskCount >= 100) {
            grantBadge(userId, "TASK_100");
        }
        return bill;
    }

    private long apply(Long userId, long amount, String bizType, String bizId, String remark) {
        PointAccount account = lockAccount(userId);
        PointLedger ledger = new PointLedger();
        ledger.setUserId(userId);
        ledger.setLedgerNo(IdUtils.uuid());
        ledger.setChangeAmount(amount);
        ledger.setBizType(bizType);
        ledger.setBizId(bizId);
        ledger.setRemark(remark);
        pointLedgerMapper.insert(ledger);
        account.setBalance(account.getBalance() + amount);
        if (amount > 0) {
            account.setTotalEarned(account.getTotalEarned() + amount);
        }
        pointAccountMapper.updateById(account);
        return amount;
    }

    private boolean existsLedger(Long userId, String bizType, String bizId) {
        long cnt = pointLedgerMapper.selectCount(new LambdaQueryWrapper<PointLedger>()
                .eq(PointLedger::getUserId, userId)
                .eq(PointLedger::getBizType, bizType)
                .eq(PointLedger::getBizId, bizId));
        return cnt > 0;
    }

    private PointAccount lockAccount(Long userId) {
        PointAccount account = pointAccountMapper.selectOne(new LambdaQueryWrapper<PointAccount>()
                .eq(PointAccount::getUserId, userId)
                .last("for update"));
        if (account == null) {
            account = new PointAccount();
            account.setUserId(userId);
            account.setBalance(0L);
            account.setTotalEarned(0L);
            pointAccountMapper.insert(account);
        }
        return account;
    }

    private boolean isRestDay(UserPreference pref, LocalDate day) {
        if (pref == null || StringUtils.isBlank(pref.getRestDays())) {
            return false;
        }
        int mark = day.getDayOfWeek().getValue() % 7;
        return pref.getRestDays().contains(String.valueOf(mark));
    }

    private LocalDate[] span(String range, LocalDate day) {
        LocalDate base = day == null ? LocalDate.now() : day;
        if ("week".equalsIgnoreCase(range)) {
            LocalDate from = base.with(DayOfWeek.MONDAY);
            return new LocalDate[]{from, from.plusDays(6)};
        }
        if ("month".equalsIgnoreCase(range)) {
            LocalDate from = base.withDayOfMonth(1);
            return new LocalDate[]{from, from.plusMonths(1).minusDays(1)};
        }
        return new LocalDate[]{base, base};
    }
}
