package com.weiji.modules.social.controller; // HTTP 接口层

import com.weiji.common.result.Result; // 本仓类 Result
import com.weiji.framework.security.Currents; // 本仓类 Currents
import com.weiji.modules.social.entity.DeskSession; // 本仓类 DeskSession
import com.weiji.modules.social.entity.FriendRequest; // 本仓类 FriendRequest
import com.weiji.modules.social.entity.PlazaPost; // 本仓类 PlazaPost
import com.weiji.modules.social.entity.Team; // 本仓类 Team
import com.weiji.modules.social.entity.TeamMember; // 本仓类 TeamMember
import com.weiji.modules.social.service.SocialService; // 本仓类 SocialService
import lombok.Data; // Lombok 样板代码生成
import lombok.RequiredArgsConstructor; // Lombok 样板代码生成
import org.springframework.web.bind.annotation.GetMapping; // Web 映射注解
import org.springframework.web.bind.annotation.PathVariable; // Web 映射注解
import org.springframework.web.bind.annotation.PostMapping; // Web 映射注解
import org.springframework.web.bind.annotation.RequestBody; // Web 映射注解
import org.springframework.web.bind.annotation.RequestMapping; // Web 映射注解
import org.springframework.web.bind.annotation.RestController; // Web 映射注解

import java.util.List; // JDK 集合/工具
import java.util.Map; // JDK 集合/工具

@RestController // REST 控制器，返回值写入 HTTP 体
@RequestMapping("/api/social") // URL 前缀
@RequiredArgsConstructor // Lombok：为 final 字段生成构造器注入
public class SocialController { // 定义类 SocialController

    private final SocialService socialService; // 构造注入 socialService

    @PostMapping("/friends/requests") // 处理 POST
    public Result<FriendRequest> request(@RequestBody IdRequest request) { // 方法 request
        return Result.ok(socialService.sendRequest(Currents.userId(), request.getUserId())); // 成功响应 code=0
    }

    @PostMapping("/friends/requests/{id}/handle") // 处理 POST
    public Result<Void> handle(@PathVariable Long id, @RequestBody HandleRequest request) { // 方法 handle
        socialService.handleRequest(Currents.userId(), id, request.isAccept()); // 取当前登录用户 ID
        return Result.ok(); // 成功响应 code=0
    }

    @GetMapping("/friends") // 处理 GET
    public Result<List<Map<String, Object>>> friends() { // 方法 friends
        return Result.ok(socialService.friendList(Currents.userId())); // 成功响应 code=0
    }

    @GetMapping("/friends/requests") // 处理 GET
    public Result<List<Map<String, Object>>> pendingRequests() { // 方法 pendingRequests
        return Result.ok(socialService.pendingRequests(Currents.userId())); // 成功响应 code=0
    }

    @GetMapping("/teams") // 处理 GET
    public Result<List<Map<String, Object>>> myTeams() { // 方法 myTeams
        return Result.ok(socialService.myTeams(Currents.userId())); // 成功响应 code=0
    }

    @PostMapping("/teams") // 处理 POST
    public Result<Team> team(@RequestBody TeamRequest request) { // 方法 team
        return Result.ok(socialService.createTeam(Currents.userId(), request.getName(), request.getGoalDesc(), request.getMemberId())); // 成功响应 code=0
    }

    @PostMapping("/teams/{id}/checkin") // 处理 POST
    public Result<TeamMember> checkin(@PathVariable Long id) { // 方法 checkin
        return Result.ok(socialService.checkin(Currents.userId(), id)); // 成功响应 code=0
    }

    @GetMapping("/teams/{id}/progress") // 处理 GET
    public Result<List<TeamMember>> progress(@PathVariable Long id) { // 方法 progress
        return Result.ok(socialService.teamProgress(id)); // 成功响应 code=0
    }

    @PostMapping("/plaza") // 处理 POST
    public Result<PlazaPost> plaza(@RequestBody PlazaPost post) { // 方法 plaza
        return Result.ok(socialService.postPlaza(Currents.userId(), post)); // 成功响应 code=0
    }

    @GetMapping("/plaza") // 处理 GET
    public Result<List<PlazaPost>> plazaList() { // 方法 plazaList
        return Result.ok(socialService.plazaList()); // 成功响应 code=0
    }

    @PostMapping("/plaza/{id}/favorite") // 处理 POST
    public Result<Void> favorite(@PathVariable Long id) { // 方法 favorite
        socialService.favoritePlaza(Currents.userId(), id); // 取当前登录用户 ID
        return Result.ok(); // 成功响应 code=0
    }

    @PostMapping("/desk") // 处理 POST
    public Result<DeskSession> desk(@RequestBody IdRequest request) { // 方法 desk
        return Result.ok(socialService.createDesk(Currents.userId(), request.getUserId())); // 成功响应 code=0
    }

    @PostMapping("/desk/{id}/start") // 处理 POST
    public Result<DeskSession> startDesk(@PathVariable Long id) { // 方法 startDesk
        return Result.ok(socialService.startDesk(Currents.userId(), id)); // 成功响应 code=0
    }

    @PostMapping("/desk/{id}/report") // 处理 POST
    public Result<DeskSession> report(@PathVariable Long id, @RequestBody DeskReport request) { // 方法 report
        return Result.ok(socialService.reportDesk(Currents.userId(), id, request.isDone(), request.getReport())); // 成功响应 code=0
    }

    @GetMapping("/desk/{id}") // 处理 GET
    public Result<DeskSession> getDesk(@PathVariable Long id) { // 方法 getDesk
        return Result.ok(socialService.getDesk(Currents.userId(), id)); // 成功响应 code=0
    }

    @GetMapping("/rank/week") // 处理 GET
    public Result<List<Map<String, Object>>> rank() { // 方法 rank
        return Result.ok(socialService.weekRank(Currents.userId())); // 成功响应 code=0
    }

    @Data // Lombok：getter/setter/equals/hashCode
    public static class IdRequest { // 定义类 IdRequest
        private Long userId; // 字段 用户 ID
    }

    @Data // Lombok：getter/setter/equals/hashCode
    public static class HandleRequest { // 定义类 HandleRequest
        private boolean accept; // 字段 accept
    }

    @Data // Lombok：getter/setter/equals/hashCode
    public static class TeamRequest { // 定义类 TeamRequest
        private String name; // 字段 name
        private String goalDesc; // 字段 goalDesc
        private Long memberId; // 字段 memberId
    }

    @Data // Lombok：getter/setter/equals/hashCode
    public static class DeskReport { // 定义类 DeskReport
        private boolean done; // 字段 done
        private String report; // 字段 report
    }
}
