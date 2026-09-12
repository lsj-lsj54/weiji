package com.weiji.modules.knowledge.service;

import com.weiji.modules.knowledge.entity.KnowledgeNote;
import com.weiji.modules.knowledge.entity.KnowledgeReview;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface KnowledgeService {

    KnowledgeNote create(Long userId, KnowledgeNote note, List<String> tags);

    KnowledgeNote update(Long userId, KnowledgeNote note, List<String> tags);

    void delete(Long userId, Long id);

    KnowledgeNote get(Long userId, Long id);

    List<KnowledgeNote> list(Long userId, String tag, String mastery, Long taskId, Boolean starred);

    KnowledgeNote merge(Long userId, Long fromId, Long toId);

    KnowledgeReview dueCard(Long userId);

    KnowledgeReview mark(Long userId, Long reviewId, boolean remembered);

    Map<String, Object> weeklySummary(Long userId, LocalDate weekStart);

    String exportText(Long userId, String tag, LocalDate from, LocalDate to);
}
