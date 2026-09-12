package com.weiji.modules.point.controller;

import com.weiji.common.result.Result;
import com.weiji.framework.security.Currents;
import com.weiji.modules.point.entity.PointLedger;
import com.weiji.modules.point.entity.Reward;
import com.weiji.modules.point.entity.RewardRedemption;
import com.weiji.modules.point.entity.UserBadge;
import com.weiji.modules.point.entity.UserItem;
import com.weiji.modules.point.service.PointService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/point")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @GetMapping("/account")
    public Result<Map<String, Object>> account() {
        return Result.ok(pointService.account(Currents.userId()));
    }

    @GetMapping("/ledgers")
    public Result<List<PointLedger>> ledgers(@RequestParam(defaultValue = "20") int limit) {
        return Result.ok(pointService.ledgers(Currents.userId(), limit));
    }

    @PostMapping("/rewards")
    public Result<Reward> createReward(@RequestBody Reward reward) {
        return Result.ok(pointService.createReward(Currents.userId(), reward));
    }

    @GetMapping("/rewards")
    public Result<List<Reward>> rewards() {
        return Result.ok(pointService.listRewards(Currents.userId()));
    }

    @PostMapping("/rewards/{id}/redeem")
    public Result<RewardRedemption> redeem(@PathVariable Long id) {
        return Result.ok(pointService.redeem(Currents.userId(), id));
    }

    @PostMapping("/donate")
    public Result<Void> donate(@RequestBody DonateRequest request) {
        pointService.spend(Currents.userId(), request.getAmount(), "CHARITY",
                java.util.UUID.randomUUID().toString(), "公益捐赠");
        return Result.ok();
    }

    @GetMapping("/items")
    public Result<List<UserItem>> items() {
        return Result.ok(pointService.items(Currents.userId()));
    }

    @GetMapping("/badges")
    public Result<List<UserBadge>> badges() {
        return Result.ok(pointService.myBadges(Currents.userId()));
    }

    @GetMapping("/time-bill")
    public Result<Map<String, Object>> timeBill(@RequestParam(defaultValue = "day") String range,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day) {
        return Result.ok(pointService.timeBill(Currents.userId(), range, day));
    }

    @GetMapping("/focus-board")
    public Result<Map<String, Object>> focusBoard(@RequestParam(defaultValue = "day") String range,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day) {
        return Result.ok(pointService.focusBoard(Currents.userId(), range, day));
    }

    @GetMapping("/evening-summary")
    public Result<Map<String, Object>> evening() {
        Map<String, Object> bill = pointService.timeBill(Currents.userId(), "day", LocalDate.now());
        int minutes = (Integer) bill.get("totalMinutes");
        bill.put("text", "今天你用碎片时间专注了 " + minutes + " 分钟，相当于省出了 "
                + bill.get("eveningsEquivalent") + " 个完整晚上。继续保持。");
        return Result.ok(bill);
    }

    @Data
    public static class DonateRequest {
        private long amount;
    }
}
