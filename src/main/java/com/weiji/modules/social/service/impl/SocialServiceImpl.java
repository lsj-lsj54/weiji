package com.weiji.modules.social.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.weiji.common.constant.RedisKey;
import com.weiji.common.enums.ErrorCode;
import com.weiji.common.exception.BizException;
import com.weiji.framework.redis.RedisUtils;
import com.weiji.modules.point.service.PointService;
import com.weiji.modules.social.entity.DeskMember;
import com.weiji.modules.social.entity.DeskSession;
import com.weiji.modules.social.entity.FriendRelation;
import com.weiji.modules.social.entity.FriendRequest;
import com.weiji.modules.social.entity.PlazaFavorite;
import com.weiji.modules.social.entity.PlazaPost;
import com.weiji.modules.social.entity.Team;
import com.weiji.modules.social.entity.TeamMember;
import com.weiji.modules.social.mapper.DeskMemberMapper;
import com.weiji.modules.social.mapper.DeskSessionMapper;
import com.weiji.modules.social.mapper.FriendRelationMapper;
import com.weiji.modules.social.mapper.FriendRequestMapper;
import com.weiji.modules.social.mapper.PlazaFavoriteMapper;
import com.weiji.modules.social.mapper.PlazaPostMapper;
import com.weiji.modules.social.mapper.TeamMapper;
import com.weiji.modules.social.mapper.TeamMemberMapper;
import com.weiji.modules.social.service.SocialService;
import com.weiji.modules.task.entity.TaskTemplate;
import com.weiji.modules.task.mapper.TaskTemplateMapper;
import com.weiji.modules.user.entity.User;
import com.weiji.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SocialServiceImpl implements SocialService {

    private final FriendRequestMapper friendRequestMapper;
    private final FriendRelationMapper friendRelationMapper;
    private final TeamMapper teamMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final PlazaPostMapper plazaPostMapper;
    private final PlazaFavoriteMapper plazaFavoriteMapper;
    private final DeskSessionMapper deskSessionMapper;
    private final DeskMemberMapper deskMemberMapper;
    private final TaskTemplateMapper taskTemplateMapper;
    private final UserMapper userMapper;
    private final PointService pointService;
    private final RedisUtils redisUtils;

    @Override
    public FriendRequest sendRequest(Long fromUserId, Long toUserId) {
        if (fromUserId.equals(toUserId)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "不能添加自己");
        }
        long exists = friendRelationMapper.selectCount(new LambdaQueryWrapper<FriendRelation>()
                .eq(FriendRelation::getUserId, fromUserId).eq(FriendRelation::getFriendId, toUserId));
        if (exists > 0) {
            throw new BizException(ErrorCode.FRIEND_EXISTS);
        }
        FriendRequest req = new FriendRequest();
        req.setFromUserId(fromUserId);
        req.setToUserId(toUserId);
        req.setStatus("PENDING");
        friendRequestMapper.insert(req);
        return req;
    }

    @Override
    @Transactional
    public void handleRequest(Long userId, Long requestId, boolean accept) {
        FriendRequest req = friendRequestMapper.selectById(requestId);
        if (req == null || !userId.equals(req.getToUserId())) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        req.setStatus(accept ? "ACCEPTED" : "REJECTED");
        friendRequestMapper.updateById(req);
        if (accept) {
            insertFriend(req.getFromUserId(), req.getToUserId());
            insertFriend(req.getToUserId(), req.getFromUserId());
        }
    }

    @Override
    public List<FriendRelation> friends(Long userId) {
        return friendRelationMapper.selectList(new LambdaQueryWrapper<FriendRelation>()
                .eq(FriendRelation::getUserId, userId));
    }

    @Override
    public List<Map<String, Object>> friendList(Long userId) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (FriendRelation rel : friends(userId)) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", rel.getId());
            row.put("userId", rel.getUserId());
            row.put("friendId", rel.getFriendId());
            User peer = userMapper.selectById(rel.getFriendId());
            row.put("nickname", peer == null ? rel.getFriendId() : peer.getNickname());
            row.put("phone", peer == null ? null : peer.getPhone());
            list.add(row);
        }
        return list;
    }

    @Override
    @Transactional
    public Team createTeam(Long ownerId, String name, String goalDesc, Long memberId) {
        Team team = new Team();
        team.setName(name);
        team.setOwnerId(ownerId);
        team.setGoalDesc(goalDesc);
        team.setStatus(1);
        teamMapper.insert(team);
        addMember(team.getId(), ownerId);
        if (memberId != null) {
            addMember(team.getId(), memberId);
        }
        return team;
    }

    @Override
    @Transactional
    public TeamMember checkin(Long userId, Long teamId) {
        TeamMember member = teamMemberMapper.selectOne(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId).eq(TeamMember::getUserId, userId));
        if (member == null) {
            throw new BizException(ErrorCode.TEAM_NOT_FOUND);
        }
        member.setFinishedCount((member.getFinishedCount() == null ? 0 : member.getFinishedCount()) + 1);
        teamMemberMapper.updateById(member);
        List<TeamMember> all = teamProgress(teamId);
        boolean everyone = all.stream().allMatch(m -> m.getFinishedCount() != null && m.getFinishedCount() > 0);
        if (everyone) {
            for (TeamMember m : all) {
                pointService.grant(m.getUserId(), 10, "MILESTONE", "team-" + teamId + "-" + LocalDate.now(), "组队全员完成");
            }
        }
        return member;
    }

    @Override
    public List<TeamMember> teamProgress(Long teamId) {
        return teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMember>().eq(TeamMember::getTeamId, teamId));
    }

    @Override
    public PlazaPost postPlaza(Long userId, PlazaPost post) {
        post.setId(null);
        post.setUserId(userId);
        if (post.getAnonymous() == null) {
            post.setAnonymous(1);
        }
        plazaPostMapper.insert(post);
        return post;
    }

    @Override
    public List<PlazaPost> plazaList() {
        return plazaPostMapper.selectList(new LambdaQueryWrapper<PlazaPost>().orderByDesc(PlazaPost::getId).last("limit 50"));
    }

    @Override
    @Transactional
    public void favoritePlaza(Long userId, Long postId) {
        PlazaPost post = plazaPostMapper.selectById(postId);
        if (post == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        PlazaFavorite fav = new PlazaFavorite();
        fav.setUserId(userId);
        fav.setPostId(postId);
        plazaFavoriteMapper.insert(fav);
        if (post.getTemplateId() != null) {
            TaskTemplate tpl = taskTemplateMapper.selectById(post.getTemplateId());
            if (tpl != null) {
                TaskTemplate copy = new TaskTemplate();
                copy.setUserId(userId);
                copy.setName(tpl.getName());
                copy.setContent(tpl.getContent());
                copy.setCategory(tpl.getCategory());
                copy.setDurationMinutes(tpl.getDurationMinutes());
                copy.setSceneCode(tpl.getSceneCode());
                copy.setSceneTags(tpl.getSceneTags());
                copy.setTimerTemplate(tpl.getTimerTemplate());
                taskTemplateMapper.insert(copy);
            }
        }
    }

    @Override
    @Transactional
    public DeskSession createDesk(Long userId, Long peerId) {
        DeskSession session = new DeskSession();
        session.setOwnerId(userId);
        session.setPeerId(peerId);
        session.setStatus("PENDING");
        deskSessionMapper.insert(session);
        addDeskMember(session.getId(), userId);
        addDeskMember(session.getId(), peerId);
        return session;
    }

    @Override
    public DeskSession startDesk(Long userId, Long sessionId) {
        DeskSession session = ownedDesk(userId, sessionId);
        session.setStatus("RUNNING");
        session.setStartedAt(LocalDateTime.now());
        deskSessionMapper.updateById(session);
        return session;
    }

    @Override
    public DeskSession reportDesk(Long userId, Long sessionId, boolean done, String report) {
        ownedDesk(userId, sessionId);
        DeskMember member = deskMemberMapper.selectOne(new LambdaQueryWrapper<DeskMember>()
                .eq(DeskMember::getSessionId, sessionId).eq(DeskMember::getUserId, userId));
        if (member == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        member.setDone(done ? 1 : 0);
        member.setReport(report);
        deskMemberMapper.updateById(member);
        List<DeskMember> members = deskMemberMapper.selectList(new LambdaQueryWrapper<DeskMember>()
                .eq(DeskMember::getSessionId, sessionId));
        boolean allDone = members.stream().allMatch(m -> Integer.valueOf(1).equals(m.getDone()));
        DeskSession session = deskSessionMapper.selectById(sessionId);
        if (allDone) {
            session.setStatus("FINISHED");
            session.setEndedAt(LocalDateTime.now());
            deskSessionMapper.updateById(session);
        }
        return session;
    }

    @Override
    public DeskSession getDesk(Long userId, Long sessionId) {
        return ownedDesk(userId, sessionId);
    }

    @Override
    public List<Map<String, Object>> weekRank(Long userId) {
        String key = RedisKey.weekRank(LocalDate.now().with(DayOfWeek.MONDAY).toString());
        List<FriendRelation> friends = friends(userId);
        List<Long> ids = new ArrayList<>();
        ids.add(userId);
        friends.forEach(f -> ids.add(f.getFriendId()));
        Set<String> ranked = redisUtils.zRevRange(key, 0, 99);
        List<Map<String, Object>> list = new ArrayList<>();
        if (ranked != null) {
            int i = 1;
            for (String member : ranked) {
                Long uid = Long.valueOf(member);
                if (!ids.contains(uid)) {
                    continue;
                }
                Map<String, Object> row = new HashMap<>();
                row.put("rank", i++);
                row.put("userId", uid);
                User user = userMapper.selectById(uid);
                row.put("nickname", user == null ? uid : user.getNickname());
                Double score = redisUtils.zScore(key, member);
                row.put("minutes", score == null ? 0 : score.intValue());
                list.add(row);
            }
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> pendingRequests(Long userId) {
        List<FriendRequest> pending = friendRequestMapper.selectList(new LambdaQueryWrapper<FriendRequest>()
                .eq(FriendRequest::getToUserId, userId)
                .eq(FriendRequest::getStatus, "PENDING")
                .orderByDesc(FriendRequest::getId));
        List<Map<String, Object>> list = new ArrayList<>();
        for (FriendRequest req : pending) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", req.getId());
            row.put("fromUserId", req.getFromUserId());
            row.put("toUserId", req.getToUserId());
            row.put("status", req.getStatus());
            User from = userMapper.selectById(req.getFromUserId());
            row.put("nickname", from == null ? req.getFromUserId() : from.getNickname());
            row.put("phone", from == null ? null : from.getPhone());
            list.add(row);
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> myTeams(Long userId) {
        List<TeamMember> memberships = teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getUserId, userId)
                .orderByDesc(TeamMember::getId));
        List<Map<String, Object>> list = new ArrayList<>();
        for (TeamMember membership : memberships) {
            Team team = teamMapper.selectById(membership.getTeamId());
            if (team == null) {
                continue;
            }
            Map<String, Object> row = new HashMap<>();
            row.put("id", team.getId());
            row.put("name", team.getName());
            row.put("goalDesc", team.getGoalDesc());
            row.put("ownerId", team.getOwnerId());
            row.put("status", team.getStatus());
            row.put("finishedCount", membership.getFinishedCount());
            row.put("members", teamProgress(team.getId()));
            list.add(row);
        }
        return list;
    }

    private void insertFriend(Long userId, Long friendId) {
        FriendRelation rel = new FriendRelation();
        rel.setUserId(userId);
        rel.setFriendId(friendId);
        friendRelationMapper.insert(rel);
    }

    private void addMember(Long teamId, Long userId) {
        TeamMember member = new TeamMember();
        member.setTeamId(teamId);
        member.setUserId(userId);
        member.setFinishedCount(0);
        teamMemberMapper.insert(member);
    }

    private void addDeskMember(Long sessionId, Long userId) {
        DeskMember member = new DeskMember();
        member.setSessionId(sessionId);
        member.setUserId(userId);
        member.setDone(0);
        deskMemberMapper.insert(member);
    }

    private DeskSession ownedDesk(Long userId, Long sessionId) {
        DeskSession session = deskSessionMapper.selectById(sessionId);
        if (session == null || (!userId.equals(session.getOwnerId()) && !userId.equals(session.getPeerId()))) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        return session;
    }
}
