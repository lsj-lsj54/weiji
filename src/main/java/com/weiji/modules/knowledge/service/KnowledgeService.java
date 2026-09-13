package com.weiji.modules.knowledge.service; // 业务接口

import com.weiji.modules.knowledge.entity.KnowledgeNote; // 本仓类 KnowledgeNote
import com.weiji.modules.knowledge.entity.KnowledgeReview; // 本仓类 KnowledgeReview

import java.time.LocalDate; // 日期时间
import java.util.List; // JDK 集合/工具
import java.util.Map; // JDK 集合/工具

public interface KnowledgeService { // 定义接口 KnowledgeService

    KnowledgeNote create(Long userId, KnowledgeNote note, List<String> tags); // 本行业务语句

    KnowledgeNote update(Long userId, KnowledgeNote note, List<String> tags); // 本行业务语句

    void delete(Long userId, Long id); // 本行业务语句

    KnowledgeNote get(Long userId, Long id); // 本行业务语句

    List<KnowledgeNote> list(Long userId, String tag, String mastery, Long taskId, Boolean starred); // 本行业务语句

    KnowledgeNote merge(Long userId, Long fromId, Long toId); // 本行业务语句

    KnowledgeReview dueCard(Long userId); // 本行业务语句

    KnowledgeReview mark(Long userId, Long reviewId, boolean remembered); // 本行业务语句

    Map<String, Object> weeklySummary(Long userId, LocalDate weekStart); // 本行业务语句

    String exportText(Long userId, String tag, LocalDate from, LocalDate to); // 本行业务语句
}
