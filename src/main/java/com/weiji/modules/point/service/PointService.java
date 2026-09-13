package com.weiji.modules.point.service; // 业务接口

import com.weiji.modules.point.entity.PointLedger; // 本仓类 PointLedger
import com.weiji.modules.point.entity.Reward; // 本仓类 Reward
import com.weiji.modules.point.entity.RewardRedemption; // 本仓类 RewardRedemption
import com.weiji.modules.point.entity.UserItem; // 本仓类 UserItem

import java.time.LocalDate; // 日期时间
import java.util.List; // JDK 集合/工具
import java.util.Map; // JDK 集合/工具

public interface PointService { // 定义接口 PointService

    long grant(Long userId, long amount, String bizType, String bizId, String remark); // 本行业务语句

    long spend(Long userId, long amount, String bizType, String bizId, String remark); // 本行业务语句

    Map<String, Object> account(Long userId); // 本行业务语句

    List<PointLedger> ledgers(Long userId, int limit); // 本行业务语句

    Reward createReward(Long userId, Reward reward); // 本行业务语句

    List<Reward> listRewards(Long userId); // 本行业务语句

    RewardRedemption redeem(Long userId, Long rewardId); // 本行业务语句

    void addItem(Long userId, String itemCode, int qty); // 本行业务语句

    boolean consumeItem(Long userId, String itemCode); // 本行业务语句

    List<UserItem> items(Long userId); // 本行业务语句

    void grantBadge(Long userId, String code); // 本行业务语句

    List<Map<String, Object>> myBadges(Long userId); // 本行业务语句

    void touchStreak(Long userId); // 本行业务语句

    Map<String, Object> timeBill(Long userId, String range, LocalDate day); // 本行业务语句

    Map<String, Object> focusBoard(Long userId, String range, LocalDate day); // 本行业务语句
}
