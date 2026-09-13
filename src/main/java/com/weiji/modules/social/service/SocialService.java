package com.weiji.modules.social.service; // 业务接口

import com.weiji.modules.social.entity.DeskSession; // 本仓类 DeskSession
import com.weiji.modules.social.entity.FriendRelation; // 本仓类 FriendRelation
import com.weiji.modules.social.entity.FriendRequest; // 本仓类 FriendRequest
import com.weiji.modules.social.entity.PlazaPost; // 本仓类 PlazaPost
import com.weiji.modules.social.entity.Team; // 本仓类 Team
import com.weiji.modules.social.entity.TeamMember; // 本仓类 TeamMember

import java.util.List; // JDK 集合/工具
import java.util.Map; // JDK 集合/工具

public interface SocialService { // 定义接口 SocialService

    FriendRequest sendRequest(Long fromUserId, Long toUserId); // 本行业务语句

    void handleRequest(Long userId, Long requestId, boolean accept); // 本行业务语句

    List<FriendRelation> friends(Long userId); // 本行业务语句

    List<Map<String, Object>> friendList(Long userId); // 本行业务语句

    Team createTeam(Long ownerId, String name, String goalDesc, Long memberId); // 本行业务语句

    TeamMember checkin(Long userId, Long teamId); // 本行业务语句

    List<TeamMember> teamProgress(Long teamId); // 本行业务语句

    PlazaPost postPlaza(Long userId, PlazaPost post); // 本行业务语句

    List<PlazaPost> plazaList(); // 本行业务语句

    void favoritePlaza(Long userId, Long postId); // 本行业务语句

    DeskSession createDesk(Long userId, Long peerId); // 本行业务语句

    DeskSession startDesk(Long userId, Long sessionId); // 本行业务语句

    DeskSession reportDesk(Long userId, Long sessionId, boolean done, String report); // 本行业务语句

    DeskSession getDesk(Long userId, Long sessionId); // 本行业务语句

    List<Map<String, Object>> weekRank(Long userId); // 本行业务语句

    List<Map<String, Object>> pendingRequests(Long userId); // 本行业务语句

    List<Map<String, Object>> myTeams(Long userId); // 本行业务语句
}
