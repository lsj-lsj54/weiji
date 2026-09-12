package com.weiji.modules.knowledge.controller;

import com.weiji.common.result.Result;
import com.weiji.framework.security.Currents;
import com.weiji.modules.knowledge.entity.KnowledgeNote;
import com.weiji.modules.knowledge.entity.KnowledgeReview;
import com.weiji.modules.knowledge.service.KnowledgeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @PostMapping
    public Result<KnowledgeNote> create(@RequestBody NoteRequest request) {
        return Result.ok(knowledgeService.create(Currents.userId(), request.getNote(), request.getTags()));
    }

    @PutMapping
    public Result<KnowledgeNote> update(@RequestBody NoteRequest request) {
        return Result.ok(knowledgeService.update(Currents.userId(), request.getNote(), request.getTags()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        knowledgeService.delete(Currents.userId(), id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result<KnowledgeNote> get(@PathVariable Long id) {
        return Result.ok(knowledgeService.get(Currents.userId(), id));
    }

    @GetMapping
    public Result<List<KnowledgeNote>> list(@RequestParam(required = false) String tag,
                                            @RequestParam(required = false) String mastery,
                                            @RequestParam(required = false) Long taskId,
                                            @RequestParam(required = false) Boolean starred) {
        return Result.ok(knowledgeService.list(Currents.userId(), tag, mastery, taskId, starred));
    }

    @PostMapping("/merge")
    public Result<KnowledgeNote> merge(@RequestBody MergeRequest request) {
        return Result.ok(knowledgeService.merge(Currents.userId(), request.getFromId(), request.getToId()));
    }

    @GetMapping("/review/due")
    public Result<KnowledgeReview> due() {
        return Result.ok(knowledgeService.dueCard(Currents.userId()));
    }

    @PostMapping("/review/{id}/mark")
    public Result<KnowledgeReview> mark(@PathVariable Long id, @RequestBody MarkRequest request) {
        return Result.ok(knowledgeService.mark(Currents.userId(), id, request.isRemembered()));
    }

    @GetMapping("/weekly-summary")
    public Result<Map<String, Object>> weekly(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from) {
        return Result.ok(knowledgeService.weeklySummary(Currents.userId(), from));
    }

    @GetMapping("/export")
    public Result<String> export(@RequestParam(required = false) String tag,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return Result.ok(knowledgeService.exportText(Currents.userId(), tag, from, to));
    }

    @Data
    public static class NoteRequest {
        private KnowledgeNote note;
        private List<String> tags;
    }

    @Data
    public static class MergeRequest {
        private Long fromId;
        private Long toId;
    }

    @Data
    public static class MarkRequest {
        private boolean remembered;
    }
}
