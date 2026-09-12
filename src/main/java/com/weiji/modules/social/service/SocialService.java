package com.weiji.modules.social.service;

public interface SocialService {

    void sendFriendRequest(Long fromUserId, Long toUserId);

    void acceptFriendRequest(Long requestId);

    Long createTeam(Long ownerId, String name);
}
