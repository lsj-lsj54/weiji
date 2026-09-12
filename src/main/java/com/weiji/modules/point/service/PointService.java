package com.weiji.modules.point.service;

import com.weiji.modules.point.entity.PointLedger;
import com.weiji.modules.point.entity.Reward;
import com.weiji.modules.point.entity.RewardRedemption;
import com.weiji.modules.point.entity.UserItem;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PointService {

    long grant(Long userId, long amount, String bizType, String bizId, String remark);

    long spend(Long userId, long amount, String bizType, String bizId, String remark);

    Map<String, Object> account(Long userId);

    List<PointLedger> ledgers(Long userId, int limit);

    Reward createReward(Long userId, Reward reward);

    List<Reward> listRewards(Long userId);

    RewardRedemption redeem(Long userId, Long rewardId);

    void addItem(Long userId, String itemCode, int qty);

    boolean consumeItem(Long userId, String itemCode);

    List<UserItem> items(Long userId);

    void grantBadge(Long userId, String code);

    List<Map<String, Object>> myBadges(Long userId);

    void touchStreak(Long userId);

    Map<String, Object> timeBill(Long userId, String range, LocalDate day);

    Map<String, Object> focusBoard(Long userId, String range, LocalDate day);
}
