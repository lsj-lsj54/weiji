package com.weiji.modules.knowledge.service.impl;

import com.weiji.modules.knowledge.entity.KnowledgeNote;
import com.weiji.modules.knowledge.service.KnowledgeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeServiceImpl implements KnowledgeService {

    @Override
    public Long createNote(Long userId, String title, String content) {
        throw new UnsupportedOperationException("知识点记录将在后续迭代实现");
    }

    @Override
    public List<KnowledgeNote> listNotes(Long userId) {
        throw new UnsupportedOperationException("知识点记录将在后续迭代实现");
    }

    @Override
    public void markReview(Long reviewId, boolean remembered) {
        throw new UnsupportedOperationException("复习调度将在后续迭代实现");
    }
}
