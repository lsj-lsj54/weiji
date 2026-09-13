package com.weiji.modules.point.controller; // HTTP 接口层

import com.weiji.common.result.Result; // 本仓类 Result
import com.weiji.framework.security.Currents; // 本仓类 Currents
import com.weiji.modules.point.entity.PointLedger; // 本仓类 PointLedger
import com.weiji.modules.point.entity.Reward; // 本仓类 Reward
import com.weiji.modules.point.entity.RewardRedemption; // 本仓类 RewardRedemption
import com.weiji.modules.point.entity.UserItem; // 本仓类 UserItem
import com.weiji.modules.point.service.PointService; // 本仓类 PointService
import lombok.Data; // Lombok 样板代码生成
import lombok.RequiredArgsConstructor; // Lombok 样板代码生成
import org.springframework.format.annotation.DateTimeFormat; // 导入 DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping; // Web 映射注解
import org.springframework.web.bind.annotation.PathVariable; // Web 映射注解
import org.springframework.web.bind.annotation.PostMapping; // Web 映射注解
import org.springframework.web.bind.annotation.RequestBody; // Web 映射注解
import org.springframework.web.bind.annotation.RequestMapping; // Web 映射注解
import org.springframework.web.bind.annotation.RequestParam; // Web 映射注解
import org.springframework.web.bind.annotation.RestController; // Web 映射注解

import java.time.LocalDate; // 日期时间
import java.util.List; // JDK 集合/工具
import java.util.Map; // JDK 集合/工具

@RestController // REST 控制器，返回值写入 HTTP 体
@RequestMapping("/api/point") // URL 前缀
@RequiredArgsConstructor // Lombok：为 final 字段生成构造器注入
public class PointController { // 定义类 PointController

    private final PointService pointService; // 构造注入 pointService

    @GetMapping("/account") // 处理 GET
    public Result<Map<String, Object>> account() { // 方法 account
        return Result.ok(pointService.account(Currents.userId())); // 成功响应 code=0
    }

    @GetMapping("/ledgers") // 处理 GET
    public Result<List<PointLedger>> ledgers(@RequestParam(defaultValue = "20") int limit) { // 方法 ledgers
        return Result.ok(pointService.ledgers(Currents.userId(), limit)); // 成功响应 code=0
    }

    @PostMapping("/rewards") // 处理 POST
    public Result<Reward> createReward(@RequestBody Reward reward) { // 方法 createReward
        return Result.ok(pointService.createReward(Currents.userId(), reward)); // 成功响应 code=0
    }

    @GetMapping("/rewards") // 处理 GET
    public Result<List<Reward>> rewards() { // 方法 rewards
        return Result.ok(pointService.listRewards(Currents.userId())); // 成功响应 code=0
    }

    @PostMapping("/rewards/{id}/redeem") // 处理 POST
    public Result<RewardRedemption> redeem(@PathVariable Long id) { // 方法 redeem
        return Result.ok(pointService.redeem(Currents.userId(), id)); // 成功响应 code=0
    }

    @PostMapping("/donate") // 处理 POST
    public Result<Void> donate(@RequestBody DonateRequest request) { // 方法 donate
        pointService.spend(Currents.userId(), request.getAmount(), "CHARITY", // 取当前登录用户 ID
                java.util.UUID.randomUUID().toString(), "公益捐赠"); // 本行业务语句
        return Result.ok(); // 成功响应 code=0
    }

    @GetMapping("/items") // 处理 GET
    public Result<List<UserItem>> items() { // 方法 items
        return Result.ok(pointService.items(Currents.userId())); // 成功响应 code=0
    }

    @GetMapping("/badges") // 处理 GET
    public Result<List<Map<String, Object>>> badges() { // 方法 badges
        return Result.ok(pointService.myBadges(Currents.userId())); // 成功响应 code=0
    }

    @GetMapping("/time-bill") // 处理 GET
    public Result<Map<String, Object>> timeBill(@RequestParam(defaultValue = "day") String range, // 赋值或调用
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day) { // 查询参数
        return Result.ok(pointService.timeBill(Currents.userId(), range, day)); // 成功响应 code=0
    }

    @GetMapping("/focus-board") // 处理 GET
    public Result<Map<String, Object>> focusBoard(@RequestParam(defaultValue = "day") String range, // 赋值或调用
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day) { // 查询参数
        return Result.ok(pointService.focusBoard(Currents.userId(), range, day)); // 成功响应 code=0
    }

    @GetMapping("/evening-summary") // 处理 GET
    public Result<Map<String, Object>> evening() { // 方法 evening
        Map<String, Object> bill = pointService.timeBill(Currents.userId(), "day", LocalDate.now()); // 取当前登录用户 ID
        int minutes = (Integer) bill.get("totalMinutes"); // 赋值或调用
        bill.put("text", "今天你用碎片时间专注了 " + minutes + " 分钟，相当于省出了 " // 本行业务语句
                + bill.get("eveningsEquivalent") + " 个完整晚上。继续保持。"); // 本行业务语句
        return Result.ok(bill); // 成功响应 code=0
    }

    @Data // Lombok：getter/setter/equals/hashCode
    public static class DonateRequest { // 定义类 DonateRequest
        private long amount; // 字段 amount
    }
}
