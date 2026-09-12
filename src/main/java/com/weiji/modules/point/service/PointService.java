package com.weiji.modules.point.service;

public interface PointService {

    void grant(Long userId, long amount, String bizType, String bizId);

    void redeem(Long userId, Long rewardId);
}
