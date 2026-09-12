package com.weiji.modules.point.service.impl;

import com.weiji.modules.point.service.PointService;
import org.springframework.stereotype.Service;

@Service
public class PointServiceImpl implements PointService {

    @Override
    public void grant(Long userId, long amount, String bizType, String bizId) {
        throw new UnsupportedOperationException("积分发放将在后续迭代实现");
    }

    @Override
    public void redeem(Long userId, Long rewardId) {
        throw new UnsupportedOperationException("奖励兑换将在后续迭代实现");
    }
}
