package com.weiji.modules.knowledge.controller; // HTTP 接口层

import com.weiji.common.result.Result; // 本仓类 Result
import com.weiji.framework.security.Currents; // 本仓类 Currents
import com.weiji.modules.knowledge.entity.KnowledgeNote; // 本仓类 KnowledgeNote
import com.weiji.modules.knowledge.entity.KnowledgeReview; // 本仓类 KnowledgeReview
import com.weiji.modules.knowledge.service.KnowledgeService; // 本仓类 KnowledgeService
import lombok.Data; // Lombok 样板代码生成
import lombok.RequiredArgsConstructor; // Lombok 样板代码生成
import org.springframework.format.annotation.DateTimeFormat; // 导入 DateTimeFormat
import org.springframework.web.bind.annotation.DeleteMapping; // Web 映射注解
import org.springframework.web.bind.annotation.GetMapping; // Web 映射注解
import org.springframework.web.bind.annotation.PathVariable; // Web 映射注解
import org.springframework.web.bind.annotation.PostMapping; // Web 映射注解
import org.springframework.web.bind.annotation.PutMapping; // Web 映射注解
import org.springframework.web.bind.annotation.RequestBody; // Web 映射注解
import org.springframework.web.bind.annotation.RequestMapping; // Web 映射注解
import org.springframework.web.bind.annotation.RequestParam; // Web 映射注解
import org.springframework.web.bind.annotation.RestController; // Web 映射注解

import java.time.LocalDate; // 日期时间
import java.util.List; // JDK 集合/工具
import java.util.Map; // JDK 集合/工具

@RestController // REST 控制器，返回值写入 HTTP 体
@RequestMapping("/api/knowledge") // URL 前缀
@RequiredArgsConstructor // Lombok：为 final 字段生成构造器注入
public class KnowledgeController { // 定义类 KnowledgeController

    private final KnowledgeService knowledgeService; // 构造注入 knowledgeService

    @PostMapping // 处理 POST
    public Result<KnowledgeNote> create(@RequestBody NoteRequest request) { // 方法 create
        return Result.ok(knowledgeService.create(Currents.userId(), request.getNote(), request.getTags())); // 成功响应 code=0
    }

    @PutMapping // 处理 PUT
    public Result<KnowledgeNote> update(@RequestBody NoteRequest request) { // 方法 update
        return Result.ok(knowledgeService.update(Currents.userId(), request.getNote(), request.getTags())); // 成功响应 code=0
    }

    @DeleteMapping("/{id}") // 处理 DELETE
    public Result<Void> delete(@PathVariable Long id) { // 方法 delete
        knowledgeService.delete(Currents.userId(), id); // 取当前登录用户 ID
        return Result.ok(); // 成功响应 code=0
    }

    @GetMapping("/{id}") // 处理 GET
    public Result<KnowledgeNote> get(@PathVariable Long id) { // 方法 get
        return Result.ok(knowledgeService.get(Currents.userId(), id)); // 成功响应 code=0
    }

    @GetMapping // 处理 GET
    public Result<List<KnowledgeNote>> list(@RequestParam(required = false) String tag, // 赋值或调用
                                            @RequestParam(required = false) String mastery, // 查询参数
                                            @RequestParam(required = false) Long taskId, // 查询参数
                                            @RequestParam(required = false) Boolean starred) { // 查询参数
        return Result.ok(knowledgeService.list(Currents.userId(), tag, mastery, taskId, starred)); // 成功响应 code=0
    }

    @PostMapping("/merge") // 处理 POST
    public Result<KnowledgeNote> merge(@RequestBody MergeRequest request) { // 方法 merge
        return Result.ok(knowledgeService.merge(Currents.userId(), request.getFromId(), request.getToId())); // 成功响应 code=0
    }

    @GetMapping("/review/due") // 处理 GET
    public Result<KnowledgeReview> due() { // 方法 due
        return Result.ok(knowledgeService.dueCard(Currents.userId())); // 成功响应 code=0
    }

    @PostMapping("/review/{id}/mark") // 处理 POST
    public Result<KnowledgeReview> mark(@PathVariable Long id, @RequestBody MarkRequest request) { // 方法 mark
        return Result.ok(knowledgeService.mark(Currents.userId(), id, request.isRemembered())); // 成功响应 code=0
    }

    @GetMapping("/weekly-summary") // 处理 GET
    public Result<Map<String, Object>> weekly(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from) { // 方法 weekly
        return Result.ok(knowledgeService.weeklySummary(Currents.userId(), from)); // 成功响应 code=0
    }

    @GetMapping("/export") // 处理 GET
    public Result<String> export(@RequestParam(required = false) String tag, // 赋值或调用
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from, // 查询参数
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) { // 查询参数
        return Result.ok(knowledgeService.exportText(Currents.userId(), tag, from, to)); // 成功响应 code=0
    }

    @Data // Lombok：getter/setter/equals/hashCode
    public static class NoteRequest { // 定义类 NoteRequest
        private KnowledgeNote note; // 字段 note
        private List<String> tags; // 字段 tags
    }

    @Data // Lombok：getter/setter/equals/hashCode
    public static class MergeRequest { // 定义类 MergeRequest
        private Long fromId; // 字段 fromId
        private Long toId; // 字段 toId
    }

    @Data // Lombok：getter/setter/equals/hashCode
    public static class MarkRequest { // 定义类 MarkRequest
        private boolean remembered; // 字段 remembered
    }
}
