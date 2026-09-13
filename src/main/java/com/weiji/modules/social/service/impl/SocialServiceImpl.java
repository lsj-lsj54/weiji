package com.weiji.modules.social.service.impl; // 业务实现

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // MyBatis-Plus
import com.weiji.common.constant.RedisKey; // 本仓类 RedisKey
import com.weiji.common.enums.ErrorCode; // 本仓类 ErrorCode
import com.weiji.common.exception.BizException; // 本仓类 BizException
import com.weiji.framework.redis.RedisUtils; // 本仓类 RedisUtils
import com.weiji.modules.point.service.PointService; // 本仓类 PointService
import com.weiji.modules.social.entity.DeskMember; // 本仓类 DeskMember
import com.weiji.modules.social.entity.DeskSession; // 本仓类 DeskSession
import com.weiji.modules.social.entity.FriendRelation; // 本仓类 FriendRelation
import com.weiji.modules.social.entity.FriendRequest; // 本仓类 FriendRequest
import com.weiji.modules.social.entity.PlazaFavorite; // 本仓类 PlazaFavorite
import com.weiji.modules.social.entity.PlazaPost; // 本仓类 PlazaPost
import com.weiji.modules.social.entity.Team; // 本仓类 Team
import com.weiji.modules.social.entity.TeamMember; // 本仓类 TeamMember
import com.weiji.modules.social.mapper.DeskMemberMapper; // 本仓类 DeskMemberMapper
import com.weiji.modules.social.mapper.DeskSessionMapper; // 本仓类 DeskSessionMapper
import com.weiji.modules.social.mapper.FriendRelationMapper; // 本仓类 FriendRelationMapper
import com.weiji.modules.social.mapper.FriendRequestMapper; // 本仓类 FriendRequestMapper
import com.weiji.modules.social.mapper.PlazaFavoriteMapper; // 本仓类 PlazaFavoriteMapper
import com.weiji.modules.social.mapper.PlazaPostMapper; // 本仓类 PlazaPostMapper
import com.weiji.modules.social.mapper.TeamMapper; // 本仓类 TeamMapper
import com.weiji.modules.social.mapper.TeamMemberMapper; // 本仓类 TeamMemberMapper
import com.weiji.modules.social.service.SocialService; // 本仓类 SocialService
import com.weiji.modules.task.entity.TaskTemplate; // 本仓类 TaskTemplate
import com.weiji.modules.task.mapper.TaskTemplateMapper; // 本仓类 TaskTemplateMapper
import com.weiji.modules.user.entity.User; // 本仓类 User
import com.weiji.modules.user.mapper.UserMapper; // 本仓类 UserMapper
import com.weiji.modules.user.service.UserService; // 本仓类 UserService
import com.weiji.modules.user.vo.UserVO; // 本仓类 UserVO
import lombok.RequiredArgsConstructor; // Lombok 样板代码生成
import org.springframework.stereotype.Service; // 业务层组件
import org.springframework.transaction.annotation.Transactional; // 事务

import java.time.DayOfWeek; // 日期时间
import java.time.LocalDate; // 日期时间
import java.time.LocalDateTime; // 日期时间
import java.util.ArrayList; // JDK 集合/工具
import java.util.HashMap; // JDK 集合/工具
import java.util.List; // JDK 集合/工具
import java.util.Map; // JDK 集合/工具
import java.util.Set; // JDK 集合/工具

@Service // 业务层
@RequiredArgsConstructor // Lombok：为 final 字段生成构造器注入
public class SocialServiceImpl implements SocialService { // 定义类 SocialServiceImpl，实现接口

    private final FriendRequestMapper friendRequestMapper; // 构造注入 friendRequestMapper
    private final FriendRelationMapper friendRelationMapper; // 构造注入 friendRelationMapper
    private final TeamMapper teamMapper; // 构造注入 teamMapper
    private final TeamMemberMapper teamMemberMapper; // 构造注入 teamMemberMapper
    private final PlazaPostMapper plazaPostMapper; // 构造注入 plazaPostMapper
    private final PlazaFavoriteMapper plazaFavoriteMapper; // 构造注入 plazaFavoriteMapper
    private final DeskSessionMapper deskSessionMapper; // 构造注入 deskSessionMapper
    private final DeskMemberMapper deskMemberMapper; // 构造注入 deskMemberMapper
    private final TaskTemplateMapper taskTemplateMapper; // 构造注入 taskTemplateMapper
    private final UserMapper userMapper; // 构造注入 userMapper
    private final UserService userService; // 构造注入 userService
    private final PointService pointService; // 构造注入 pointService
    private final RedisUtils redisUtils; // 构造注入 Redis 封装

    @Override // 覆盖父类/接口方法
    public FriendRequest sendRequest(Long fromUserId, Long toUserId) { // 方法 sendRequest
        if (fromUserId.equals(toUserId)) { // 条件判断
            throw new BizException(ErrorCode.BAD_REQUEST, "不能添加自己"); // 抛出业务或运行时异常
        }
        long exists = friendRelationMapper.selectCount(new LambdaQueryWrapper<FriendRelation>() // 计数
                .eq(FriendRelation::getUserId, fromUserId).eq(FriendRelation::getFriendId, toUserId)); // 本行业务语句
        if (exists > 0) { // 条件判断
            throw new BizException(ErrorCode.FRIEND_EXISTS); // 抛出业务或运行时异常
        }
        FriendRequest req = new FriendRequest(); // 赋值或调用
        req.setFromUserId(fromUserId); // 本行业务语句
        req.setToUserId(toUserId); // 本行业务语句
        req.setStatus("PENDING"); // 本行业务语句
        friendRequestMapper.insert(req); // 插入一行
        return req; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    @Transactional // 声明式事务，异常则回滚
    public void handleRequest(Long userId, Long requestId, boolean accept) { // 方法 handleRequest
        FriendRequest req = friendRequestMapper.selectById(requestId); // 按主键查库
        if (req == null || !userId.equals(req.getToUserId())) { // 条件判断
            throw new BizException(ErrorCode.NOT_FOUND); // 抛出业务或运行时异常
        }
        req.setStatus(accept ? "ACCEPTED" : "REJECTED"); // 本行业务语句
        friendRequestMapper.updateById(req); // 按主键更新
        if (accept) { // 条件判断
            insertFriend(req.getFromUserId(), req.getToUserId()); // 本行业务语句
            insertFriend(req.getToUserId(), req.getFromUserId()); // 本行业务语句
        }
    }

    @Override // 覆盖父类/接口方法
    public List<FriendRelation> friends(Long userId) { // 方法 friends
        return friendRelationMapper.selectList(new LambdaQueryWrapper<FriendRelation>() // 查列表
                .eq(FriendRelation::getUserId, userId)); // 本行业务语句
    }

    @Override // 覆盖父类/接口方法
    public List<Map<String, Object>> friendList(Long userId) { // 方法 friendList
        List<Map<String, Object>> list = new ArrayList<>(); // 赋值或调用
        for (FriendRelation rel : friends(userId)) { // 循环
            Map<String, Object> row = new HashMap<>(); // 赋值或调用
            row.put("id", rel.getId()); // 本行业务语句
            row.put("userId", rel.getUserId()); // 本行业务语句
            row.put("friendId", rel.getFriendId()); // 本行业务语句
            User peer = userMapper.selectById(rel.getFriendId()); // 按主键查库
            row.put("nickname", peer == null ? rel.getFriendId() : peer.getNickname()); // 赋值或调用
            row.put("phone", peer == null ? null : peer.getPhone()); // 赋值或调用
            list.add(row); // 本行业务语句
        }
        return list; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    @Transactional // 声明式事务，异常则回滚
    public Team createTeam(Long ownerId, String name, String goalDesc, Long memberId) { // 方法 createTeam
        Team team = new Team(); // 赋值或调用
        team.setName(name); // 本行业务语句
        team.setOwnerId(ownerId); // 本行业务语句
        team.setGoalDesc(goalDesc); // 本行业务语句
        team.setStatus(1); // 本行业务语句
        teamMapper.insert(team); // 插入一行
        addMember(team.getId(), ownerId); // 本行业务语句
        if (memberId != null) { // 条件判断
            addMember(team.getId(), memberId); // 本行业务语句
        }
        return team; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    @Transactional // 声明式事务，异常则回滚
    public TeamMember checkin(Long userId, Long teamId) { // 方法 checkin
        TeamMember member = teamMemberMapper.selectOne(new LambdaQueryWrapper<TeamMember>() // 查一条
                .eq(TeamMember::getTeamId, teamId).eq(TeamMember::getUserId, userId)); // 本行业务语句
        if (member == null) { // 条件判断
            throw new BizException(ErrorCode.TEAM_NOT_FOUND); // 抛出业务或运行时异常
        }
        member.setFinishedCount((member.getFinishedCount() == null ? 0 : member.getFinishedCount()) + 1); // 赋值或调用
        teamMemberMapper.updateById(member); // 按主键更新
        List<TeamMember> all = teamProgress(teamId); // 赋值或调用
        boolean everyone = all.stream().allMatch(m -> m.getFinishedCount() != null && m.getFinishedCount() > 0); // 赋值或调用
        if (everyone) { // 条件判断
            for (TeamMember m : all) { // 循环
                pointService.grant(m.getUserId(), 10, "MILESTONE", "team-" + teamId + "-" + LocalDate.now(), "组队全员完成"); // 本行业务语句
            }
        }
        return member; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public List<TeamMember> teamProgress(Long teamId) { // 方法 teamProgress
        return teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMember>().eq(TeamMember::getTeamId, teamId)); // 查列表
    }

    @Override // 覆盖父类/接口方法
    public PlazaPost postPlaza(Long userId, PlazaPost post) { // 方法 postPlaza
        post.setId(null); // 本行业务语句
        post.setUserId(userId); // 本行业务语句
        if (post.getAnonymous() == null) { // 条件判断
            post.setAnonymous(1); // 本行业务语句
        }
        plazaPostMapper.insert(post); // 插入一行
        return post; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public List<PlazaPost> plazaList() { // 方法 plazaList
        return plazaPostMapper.selectList(new LambdaQueryWrapper<PlazaPost>().orderByDesc(PlazaPost::getId).last("limit 50")); // 查列表
    }

    @Override // 覆盖父类/接口方法
    @Transactional // 声明式事务，异常则回滚
    public void favoritePlaza(Long userId, Long postId) { // 方法 favoritePlaza
        PlazaPost post = plazaPostMapper.selectById(postId); // 按主键查库
        if (post == null) { // 条件判断
            throw new BizException(ErrorCode.NOT_FOUND); // 抛出业务或运行时异常
        }
        PlazaFavorite fav = new PlazaFavorite(); // 赋值或调用
        fav.setUserId(userId); // 本行业务语句
        fav.setPostId(postId); // 本行业务语句
        plazaFavoriteMapper.insert(fav); // 插入一行
        if (post.getTemplateId() != null) { // 条件判断
            TaskTemplate tpl = taskTemplateMapper.selectById(post.getTemplateId()); // 按主键查库
            if (tpl != null) { // 条件判断
                TaskTemplate copy = new TaskTemplate(); // 赋值或调用
                copy.setUserId(userId); // 本行业务语句
                copy.setName(tpl.getName()); // 本行业务语句
                copy.setContent(tpl.getContent()); // 本行业务语句
                copy.setCategory(tpl.getCategory()); // 本行业务语句
                copy.setDurationMinutes(tpl.getDurationMinutes()); // 本行业务语句
                copy.setSceneCode(tpl.getSceneCode()); // 本行业务语句
                copy.setSceneTags(tpl.getSceneTags()); // 本行业务语句
                copy.setTimerTemplate(tpl.getTimerTemplate()); // 本行业务语句
                taskTemplateMapper.insert(copy); // 插入一行
            }
        }
    }

    @Override // 覆盖父类/接口方法
    @Transactional // 声明式事务，异常则回滚
    public DeskSession createDesk(Long userId, Long peerId) { // 方法 createDesk
        DeskSession session = new DeskSession(); // 赋值或调用
        session.setOwnerId(userId); // 本行业务语句
        session.setPeerId(peerId); // 本行业务语句
        session.setStatus("PENDING"); // 本行业务语句
        deskSessionMapper.insert(session); // 插入一行
        addDeskMember(session.getId(), userId); // 本行业务语句
        addDeskMember(session.getId(), peerId); // 本行业务语句
        return session; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public DeskSession startDesk(Long userId, Long sessionId) { // 方法 startDesk
        DeskSession session = ownedDesk(userId, sessionId); // 赋值或调用
        session.setStatus("RUNNING"); // 本行业务语句
        session.setStartedAt(LocalDateTime.now()); // 本行业务语句
        deskSessionMapper.updateById(session); // 按主键更新
        return session; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public DeskSession reportDesk(Long userId, Long sessionId, boolean done, String report) { // 方法 reportDesk
        ownedDesk(userId, sessionId); // 本行业务语句
        DeskMember member = deskMemberMapper.selectOne(new LambdaQueryWrapper<DeskMember>() // 查一条
                .eq(DeskMember::getSessionId, sessionId).eq(DeskMember::getUserId, userId)); // 本行业务语句
        if (member == null) { // 条件判断
            throw new BizException(ErrorCode.NOT_FOUND); // 抛出业务或运行时异常
        }
        member.setDone(done ? 1 : 0); // 本行业务语句
        member.setReport(report); // 本行业务语句
        deskMemberMapper.updateById(member); // 按主键更新
        List<DeskMember> members = deskMemberMapper.selectList(new LambdaQueryWrapper<DeskMember>() // 查列表
                .eq(DeskMember::getSessionId, sessionId)); // 本行业务语句
        boolean allDone = members.stream().allMatch(m -> Integer.valueOf(1).equals(m.getDone())); // 赋值或调用
        DeskSession session = deskSessionMapper.selectById(sessionId); // 按主键查库
        if (allDone) { // 条件判断
            session.setStatus("FINISHED"); // 本行业务语句
            session.setEndedAt(LocalDateTime.now()); // 本行业务语句
            deskSessionMapper.updateById(session); // 按主键更新
        }
        return session; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public DeskSession getDesk(Long userId, Long sessionId) { // 方法 getDesk
        return ownedDesk(userId, sessionId); // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public List<Map<String, Object>> weekRank(Long userId) { // 方法 weekRank
        String key = RedisKey.weekRank(LocalDate.now().with(DayOfWeek.MONDAY).toString()); // 赋值或调用
        List<FriendRelation> friends = friends(userId); // 赋值或调用
        List<Long> ids = new ArrayList<>(); // 赋值或调用
        ids.add(userId); // 本行业务语句
        friends.forEach(f -> ids.add(f.getFriendId())); // 本行业务语句
        Set<String> ranked = redisUtils.zRevRange(key, 0, 99); // 调用 Redis 封装
        List<Map<String, Object>> list = new ArrayList<>(); // 赋值或调用
        if (ranked != null) { // 条件判断
            int i = 1; // 赋值或调用
            for (String member : ranked) { // 循环
                Long uid = Long.valueOf(member); // 赋值或调用
                if (!ids.contains(uid)) { // 条件判断
                    continue; // 本行业务语句
                }
                Map<String, Object> row = new HashMap<>(); // 赋值或调用
                row.put("rank", i++); // 本行业务语句
                row.put("userId", uid); // 本行业务语句
                UserVO user = userService.getUser(uid); // 赋值或调用
                row.put("nickname", user == null ? uid : user.getNickname()); // 赋值或调用
                Double score = redisUtils.zScore(key, member); // 调用 Redis 封装
                row.put("minutes", score == null ? 0 : score.intValue()); // 赋值或调用
                list.add(row); // 本行业务语句
            }
        }
        return list; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public List<Map<String, Object>> pendingRequests(Long userId) { // 方法 pendingRequests
        List<FriendRequest> pending = friendRequestMapper.selectList(new LambdaQueryWrapper<FriendRequest>() // 查列表
                .eq(FriendRequest::getToUserId, userId) // 本行业务语句
                .eq(FriendRequest::getStatus, "PENDING") // 本行业务语句
                .orderByDesc(FriendRequest::getId)); // 本行业务语句
        List<Map<String, Object>> list = new ArrayList<>(); // 赋值或调用
        for (FriendRequest req : pending) { // 循环
            Map<String, Object> row = new HashMap<>(); // 赋值或调用
            row.put("id", req.getId()); // 本行业务语句
            row.put("fromUserId", req.getFromUserId()); // 本行业务语句
            row.put("toUserId", req.getToUserId()); // 本行业务语句
            row.put("status", req.getStatus()); // 本行业务语句
            User from = userMapper.selectById(req.getFromUserId()); // 按主键查库
            row.put("nickname", from == null ? req.getFromUserId() : from.getNickname()); // 赋值或调用
            row.put("phone", from == null ? null : from.getPhone()); // 赋值或调用
            list.add(row); // 本行业务语句
        }
        return list; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public List<Map<String, Object>> myTeams(Long userId) { // 方法 myTeams
        List<TeamMember> memberships = teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMember>() // 查列表
                .eq(TeamMember::getUserId, userId) // 本行业务语句
                .orderByDesc(TeamMember::getId)); // 本行业务语句
        List<Map<String, Object>> list = new ArrayList<>(); // 赋值或调用
        for (TeamMember membership : memberships) { // 循环
            Team team = teamMapper.selectById(membership.getTeamId()); // 按主键查库
            if (team == null) { // 条件判断
                continue; // 本行业务语句
            }
            Map<String, Object> row = new HashMap<>(); // 赋值或调用
            row.put("id", team.getId()); // 本行业务语句
            row.put("name", team.getName()); // 本行业务语句
            row.put("goalDesc", team.getGoalDesc()); // 本行业务语句
            row.put("ownerId", team.getOwnerId()); // 本行业务语句
            row.put("status", team.getStatus()); // 本行业务语句
            row.put("finishedCount", membership.getFinishedCount()); // 本行业务语句
            row.put("members", teamProgress(team.getId())); // 本行业务语句
            list.add(row); // 本行业务语句
        }
        return list; // 返回结果
    }

    private void insertFriend(Long userId, Long friendId) { // 方法 insertFriend
        FriendRelation rel = new FriendRelation(); // 赋值或调用
        rel.setUserId(userId); // 本行业务语句
        rel.setFriendId(friendId); // 本行业务语句
        friendRelationMapper.insert(rel); // 插入一行
    }

    private void addMember(Long teamId, Long userId) { // 方法 addMember
        TeamMember member = new TeamMember(); // 赋值或调用
        member.setTeamId(teamId); // 本行业务语句
        member.setUserId(userId); // 本行业务语句
        member.setFinishedCount(0); // 本行业务语句
        teamMemberMapper.insert(member); // 插入一行
    }

    private void addDeskMember(Long sessionId, Long userId) { // 方法 addDeskMember
        DeskMember member = new DeskMember(); // 赋值或调用
        member.setSessionId(sessionId); // 本行业务语句
        member.setUserId(userId); // 本行业务语句
        member.setDone(0); // 本行业务语句
        deskMemberMapper.insert(member); // 插入一行
    }

    private DeskSession ownedDesk(Long userId, Long sessionId) { // 方法 ownedDesk
        DeskSession session = deskSessionMapper.selectById(sessionId); // 按主键查库
        if (session == null || (!userId.equals(session.getOwnerId()) && !userId.equals(session.getPeerId()))) { // 条件判断
            throw new BizException(ErrorCode.NOT_FOUND); // 抛出业务或运行时异常
        }
        return session; // 返回结果
    }
}
