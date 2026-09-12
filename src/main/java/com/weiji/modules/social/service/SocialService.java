package com.weiji.modules.social.service;

import com.weiji.modules.social.entity.DeskSession;
import com.weiji.modules.social.entity.FriendRelation;
import com.weiji.modules.social.entity.FriendRequest;
import com.weiji.modules.social.entity.PlazaPost;
import com.weiji.modules.social.entity.Team;
import com.weiji.modules.social.entity.TeamMember;

import java.util.List;
import java.util.Map;

public interface SocialService {

    FriendRequest sendRequest(Long fromUserId, Long toUserId);

    void handleRequest(Long userId, Long requestId, boolean accept);

    List<FriendRelation> friends(Long userId);

    Team createTeam(Long ownerId, String name, String goalDesc, Long memberId);

    TeamMember checkin(Long userId, Long teamId);

    List<TeamMember> teamProgress(Long teamId);

    PlazaPost postPlaza(Long userId, PlazaPost post);

    List<PlazaPost> plazaList();

    void favoritePlaza(Long userId, Long postId);

    DeskSession createDesk(Long userId, Long peerId);

    DeskSession startDesk(Long userId, Long sessionId);

    DeskSession reportDesk(Long userId, Long sessionId, boolean done, String report);

    DeskSession getDesk(Long userId, Long sessionId);

    List<Map<String, Object>> weekRank(Long userId);
}
