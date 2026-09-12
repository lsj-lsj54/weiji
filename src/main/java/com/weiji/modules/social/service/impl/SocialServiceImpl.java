package com.weiji.modules.social.service.impl;

import com.weiji.modules.social.service.SocialService;
import org.springframework.stereotype.Service;

@Service
public class SocialServiceImpl implements SocialService {

    @Override
    public void sendFriendRequest(Long fromUserId, Long toUserId) {
        throw new UnsupportedOperationException("好友申请将在后续迭代实现");
    }

    @Override
    public void acceptFriendRequest(Long requestId) {
        throw new UnsupportedOperationException("好友申请将在后续迭代实现");
    }

    @Override
    public Long createTeam(Long ownerId, String name) {
        throw new UnsupportedOperationException("组队打卡将在后续迭代实现");
    }
}
