package com.weiji.modules.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.weiji.common.enums.ErrorCode;
import com.weiji.common.exception.BizException;
import com.weiji.modules.knowledge.entity.KnowledgeNote;
import com.weiji.modules.knowledge.entity.KnowledgeNoteTag;
import com.weiji.modules.knowledge.entity.KnowledgeReview;
import com.weiji.modules.knowledge.entity.KnowledgeTag;
import com.weiji.modules.knowledge.mapper.KnowledgeNoteMapper;
import com.weiji.modules.knowledge.mapper.KnowledgeNoteTagMapper;
import com.weiji.modules.knowledge.mapper.KnowledgeReviewMapper;
import com.weiji.modules.knowledge.mapper.KnowledgeTagMapper;
import com.weiji.modules.knowledge.service.KnowledgeService;
import com.weiji.modules.knowledge.service.review.EbbinghausReviewScheduler;
import com.weiji.modules.task.entity.Task;
import com.weiji.modules.task.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    private final KnowledgeNoteMapper noteMapper;
    private final KnowledgeTagMapper tagMapper;
    private final KnowledgeNoteTagMapper noteTagMapper;
    private final KnowledgeReviewMapper reviewMapper;
    private final TaskMapper taskMapper;
    private final EbbinghausReviewScheduler scheduler;

    @Override
    @Transactional
    public KnowledgeNote create(Long userId, KnowledgeNote note, List<String> tags) {
        note.setId(null);
        note.setUserId(userId);
        if (StringUtils.isBlank(note.getMediaType())) {
            note.setMediaType("TEXT");
        }
        if (StringUtils.isBlank(note.getMastery())) {
            note.setMastery("UNKNOWN");
        }
        if (note.getStarred() == null) {
            note.setStarred(0);
        }
        noteMapper.insert(note);
        bindTags(userId, note.getId(), tags);
        KnowledgeReview review = new KnowledgeReview();
        review.setUserId(userId);
        review.setNoteId(note.getId());
        review.setIntervalDays(1);
        review.setNextReviewAt(LocalDateTime.now().plusDays(1));
        reviewMapper.insert(review);
        pushReviewTask(userId, note);
        return note;
    }

    @Override
    @Transactional
    public KnowledgeNote update(Long userId, KnowledgeNote note, List<String> tags) {
        KnowledgeNote old = owned(userId, note.getId());
        note.setUserId(old.getUserId());
        noteMapper.updateById(note);
        if (tags != null) {
            noteTagMapper.delete(new LambdaQueryWrapper<KnowledgeNoteTag>().eq(KnowledgeNoteTag::getNoteId, note.getId()));
            bindTags(userId, note.getId(), tags);
        }
        return noteMapper.selectById(note.getId());
    }

    @Override
    public void delete(Long userId, Long id) {
        owned(userId, id);
        noteMapper.deleteById(id);
        noteTagMapper.delete(new LambdaQueryWrapper<KnowledgeNoteTag>().eq(KnowledgeNoteTag::getNoteId, id));
    }

    @Override
    public KnowledgeNote get(Long userId, Long id) {
        return owned(userId, id);
    }

    @Override
    public List<KnowledgeNote> list(Long userId, String tag, String mastery, Long taskId, Boolean starred) {
        List<Long> ids = null;
        if (StringUtils.isNotBlank(tag)) {
            KnowledgeTag kt = tagMapper.selectOne(new LambdaQueryWrapper<KnowledgeTag>()
                    .eq(KnowledgeTag::getUserId, userId).eq(KnowledgeTag::getName, tag));
            if (kt == null) {
                return List.of();
            }
            ids = noteTagMapper.selectList(new LambdaQueryWrapper<KnowledgeNoteTag>()
                            .eq(KnowledgeNoteTag::getTagId, kt.getId()))
                    .stream().map(KnowledgeNoteTag::getNoteId).toList();
            if (ids.isEmpty()) {
                return List.of();
            }
        }
        return noteMapper.selectList(new LambdaQueryWrapper<KnowledgeNote>()
                .eq(KnowledgeNote::getUserId, userId)
                .eq(StringUtils.isNotBlank(mastery), KnowledgeNote::getMastery, mastery)
                .eq(taskId != null, KnowledgeNote::getTaskId, taskId)
                .eq(Boolean.TRUE.equals(starred), KnowledgeNote::getStarred, 1)
                .in(ids != null, KnowledgeNote::getId, ids)
                .orderByDesc(KnowledgeNote::getId));
    }

    @Override
    @Transactional
    public KnowledgeNote merge(Long userId, Long fromId, Long toId) {
        KnowledgeNote from = owned(userId, fromId);
        KnowledgeNote to = owned(userId, toId);
        String content = StringUtils.defaultString(to.getContent()) + "\n" + StringUtils.defaultString(from.getContent());
        to.setContent(content.trim());
        noteMapper.updateById(to);
        noteMapper.deleteById(fromId);
        return to;
    }

    @Override
    public KnowledgeReview dueCard(Long userId) {
        return reviewMapper.selectOne(new LambdaQueryWrapper<KnowledgeReview>()
                .eq(KnowledgeReview::getUserId, userId)
                .le(KnowledgeReview::getNextReviewAt, LocalDateTime.now())
                .orderByAsc(KnowledgeReview::getNextReviewAt)
                .last("limit 1"));
    }

    @Override
    @Transactional
    public KnowledgeReview mark(Long userId, Long reviewId, boolean remembered) {
        KnowledgeReview review = reviewMapper.selectById(reviewId);
        if (review == null || !userId.equals(review.getUserId())) {
            throw new BizException(ErrorCode.KNOWLEDGE_NOT_FOUND);
        }
        int next = scheduler.nextInterval(review.getIntervalDays() == null ? 1 : review.getIntervalDays(), remembered);
        review.setRemembered(remembered ? 1 : 0);
        review.setIntervalDays(next);
        review.setNextReviewAt(LocalDateTime.now().plusDays(next));
        reviewMapper.updateById(review);
        KnowledgeNote note = noteMapper.selectById(review.getNoteId());
        if (note != null) {
            note.setMastery(remembered ? "MASTERED" : "UNKNOWN");
            noteMapper.updateById(note);
            if (!remembered) {
                pushReviewTask(userId, note);
            }
        }
        return review;
    }

    @Override
    public Map<String, Object> weeklySummary(Long userId, LocalDate weekStart) {
        LocalDate start = weekStart == null ? LocalDate.now().minusDays(6) : weekStart;
        List<KnowledgeNote> notes = noteMapper.selectList(new LambdaQueryWrapper<KnowledgeNote>()
                .eq(KnowledgeNote::getUserId, userId)
                .ge(KnowledgeNote::getCreatedAt, start.atStartOfDay())
                .lt(KnowledgeNote::getCreatedAt, start.plusDays(7).atStartOfDay()));
        Map<String, List<String>> grouped = new LinkedHashMap<>();
        for (KnowledgeNote note : notes) {
            List<KnowledgeNoteTag> rel = noteTagMapper.selectList(new LambdaQueryWrapper<KnowledgeNoteTag>()
                    .eq(KnowledgeNoteTag::getNoteId, note.getId()));
            if (rel.isEmpty()) {
                grouped.computeIfAbsent("未分类", k -> new ArrayList<>()).add(note.getTitle());
            } else {
                for (KnowledgeNoteTag r : rel) {
                    KnowledgeTag tag = tagMapper.selectById(r.getTagId());
                    String name = tag == null ? "未分类" : tag.getName();
                    grouped.computeIfAbsent(name, k -> new ArrayList<>()).add(note.getTitle());
                }
            }
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("from", start);
        map.put("count", notes.size());
        map.put("byTag", grouped);
        return map;
    }

    @Override
    public String exportText(Long userId, String tag, LocalDate from, LocalDate to) {
        List<KnowledgeNote> notes = list(userId, tag, null, null, null);
        return notes.stream()
                .filter(n -> from == null || n.getCreatedAt() == null || !n.getCreatedAt().toLocalDate().isBefore(from))
                .filter(n -> to == null || n.getCreatedAt() == null || !n.getCreatedAt().toLocalDate().isAfter(to))
                .map(n -> "# " + n.getTitle() + "\n" + StringUtils.defaultString(n.getContent()))
                .collect(Collectors.joining("\n\n"));
    }

    private void bindTags(Long userId, Long noteId, List<String> tags) {
        if (tags == null) {
            return;
        }
        for (String name : tags) {
            if (StringUtils.isBlank(name)) {
                continue;
            }
            KnowledgeTag tag = tagMapper.selectOne(new LambdaQueryWrapper<KnowledgeTag>()
                    .eq(KnowledgeTag::getUserId, userId).eq(KnowledgeTag::getName, name.trim()));
            if (tag == null) {
                tag = new KnowledgeTag();
                tag.setUserId(userId);
                tag.setName(name.trim());
                tagMapper.insert(tag);
            }
            KnowledgeNoteTag rel = new KnowledgeNoteTag();
            rel.setNoteId(noteId);
            rel.setTagId(tag.getId());
            noteTagMapper.insert(rel);
        }
    }

    private void pushReviewTask(Long userId, KnowledgeNote note) {
        Task task = new Task();
        task.setUserId(userId);
        task.setTitle("复习：" + note.getTitle());
        task.setContent(StringUtils.abbreviate(note.getContent(), 200));
        task.setCategory("学习");
        task.setPriority(0);
        task.setDurationMinutes(10);
        task.setStatus(0);
        task.setBuffered(0);
        task.setSourceType("REVIEW");
        task.setReviewNoteId(note.getId());
        taskMapper.insert(task);
    }

    private KnowledgeNote owned(Long userId, Long id) {
        KnowledgeNote note = noteMapper.selectById(id);
        if (note == null || !Objects.equals(userId, note.getUserId())) {
            throw new BizException(ErrorCode.KNOWLEDGE_NOT_FOUND);
        }
        return note;
    }
}
