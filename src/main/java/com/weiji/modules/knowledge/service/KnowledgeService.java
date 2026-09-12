package com.weiji.modules.knowledge.service;

import com.weiji.modules.knowledge.entity.KnowledgeNote;

import java.util.List;

public interface KnowledgeService {

    Long createNote(Long userId, String title, String content);

    List<KnowledgeNote> listNotes(Long userId);

    void markReview(Long reviewId, boolean remembered);
}
