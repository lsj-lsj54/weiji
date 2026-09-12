package com.weiji.modules.social.controller;

import com.weiji.common.result.Result;
import com.weiji.framework.security.Currents;
import com.weiji.modules.social.entity.DeskSession;
import com.weiji.modules.social.entity.FriendRequest;
import com.weiji.modules.social.entity.PlazaPost;
import com.weiji.modules.social.entity.Team;
import com.weiji.modules.social.entity.TeamMember;
import com.weiji.modules.social.service.SocialService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
public class SocialController {

    private final SocialService socialService;

    @PostMapping("/friends/requests")
    public Result<FriendRequest> request(@RequestBody IdRequest request) {
        return Result.ok(socialService.sendRequest(Currents.userId(), request.getUserId()));
    }

    @PostMapping("/friends/requests/{id}/handle")
    public Result<Void> handle(@PathVariable Long id, @RequestBody HandleRequest request) {
        socialService.handleRequest(Currents.userId(), id, request.isAccept());
        return Result.ok();
    }

    @GetMapping("/friends")
    public Result<List<Map<String, Object>>> friends() {
        return Result.ok(socialService.friendList(Currents.userId()));
    }

    @GetMapping("/friends/requests")
    public Result<List<Map<String, Object>>> pendingRequests() {
        return Result.ok(socialService.pendingRequests(Currents.userId()));
    }

    @GetMapping("/teams")
    public Result<List<Map<String, Object>>> myTeams() {
        return Result.ok(socialService.myTeams(Currents.userId()));
    }

    @PostMapping("/teams")
    public Result<Team> team(@RequestBody TeamRequest request) {
        return Result.ok(socialService.createTeam(Currents.userId(), request.getName(), request.getGoalDesc(), request.getMemberId()));
    }

    @PostMapping("/teams/{id}/checkin")
    public Result<TeamMember> checkin(@PathVariable Long id) {
        return Result.ok(socialService.checkin(Currents.userId(), id));
    }

    @GetMapping("/teams/{id}/progress")
    public Result<List<TeamMember>> progress(@PathVariable Long id) {
        return Result.ok(socialService.teamProgress(id));
    }

    @PostMapping("/plaza")
    public Result<PlazaPost> plaza(@RequestBody PlazaPost post) {
        return Result.ok(socialService.postPlaza(Currents.userId(), post));
    }

    @GetMapping("/plaza")
    public Result<List<PlazaPost>> plazaList() {
        return Result.ok(socialService.plazaList());
    }

    @PostMapping("/plaza/{id}/favorite")
    public Result<Void> favorite(@PathVariable Long id) {
        socialService.favoritePlaza(Currents.userId(), id);
        return Result.ok();
    }

    @PostMapping("/desk")
    public Result<DeskSession> desk(@RequestBody IdRequest request) {
        return Result.ok(socialService.createDesk(Currents.userId(), request.getUserId()));
    }

    @PostMapping("/desk/{id}/start")
    public Result<DeskSession> startDesk(@PathVariable Long id) {
        return Result.ok(socialService.startDesk(Currents.userId(), id));
    }

    @PostMapping("/desk/{id}/report")
    public Result<DeskSession> report(@PathVariable Long id, @RequestBody DeskReport request) {
        return Result.ok(socialService.reportDesk(Currents.userId(), id, request.isDone(), request.getReport()));
    }

    @GetMapping("/desk/{id}")
    public Result<DeskSession> getDesk(@PathVariable Long id) {
        return Result.ok(socialService.getDesk(Currents.userId(), id));
    }

    @GetMapping("/rank/week")
    public Result<List<Map<String, Object>>> rank() {
        return Result.ok(socialService.weekRank(Currents.userId()));
    }

    @Data
    public static class IdRequest {
        private Long userId;
    }

    @Data
    public static class HandleRequest {
        private boolean accept;
    }

    @Data
    public static class TeamRequest {
        private String name;
        private String goalDesc;
        private Long memberId;
    }

    @Data
    public static class DeskReport {
        private boolean done;
        private String report;
    }
}
