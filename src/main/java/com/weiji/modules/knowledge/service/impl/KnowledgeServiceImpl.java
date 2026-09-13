package com.weiji.modules.knowledge.service.impl; // 业务实现

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // MyBatis-Plus
import com.weiji.common.enums.ErrorCode; // 本仓类 ErrorCode
import com.weiji.common.exception.BizException; // 本仓类 BizException
import com.weiji.modules.knowledge.entity.KnowledgeNote; // 本仓类 KnowledgeNote
import com.weiji.modules.knowledge.entity.KnowledgeNoteTag; // 本仓类 KnowledgeNoteTag
import com.weiji.modules.knowledge.entity.KnowledgeReview; // 本仓类 KnowledgeReview
import com.weiji.modules.knowledge.entity.KnowledgeTag; // 本仓类 KnowledgeTag
import com.weiji.modules.knowledge.mapper.KnowledgeNoteMapper; // 本仓类 KnowledgeNoteMapper
import com.weiji.modules.knowledge.mapper.KnowledgeNoteTagMapper; // 本仓类 KnowledgeNoteTagMapper
import com.weiji.modules.knowledge.mapper.KnowledgeReviewMapper; // 本仓类 KnowledgeReviewMapper
import com.weiji.modules.knowledge.mapper.KnowledgeTagMapper; // 本仓类 KnowledgeTagMapper
import com.weiji.modules.knowledge.service.KnowledgeService; // 本仓类 KnowledgeService
import com.weiji.modules.knowledge.service.review.EbbinghausReviewScheduler; // 本仓类 EbbinghausReviewScheduler
import com.weiji.modules.task.entity.Task; // 本仓类 Task
import com.weiji.modules.task.mapper.TaskMapper; // 本仓类 TaskMapper
import lombok.RequiredArgsConstructor; // Lombok 样板代码生成
import org.apache.commons.lang3.StringUtils; // 字符串工具
import org.springframework.stereotype.Service; // 业务层组件
import org.springframework.transaction.annotation.Transactional; // 事务

import java.time.LocalDate; // 日期时间
import java.time.LocalDateTime; // 日期时间
import java.util.ArrayList; // JDK 集合/工具
import java.util.LinkedHashMap; // JDK 集合/工具
import java.util.List; // JDK 集合/工具
import java.util.Map; // JDK 集合/工具
import java.util.Objects; // JDK 集合/工具
import java.util.stream.Collectors; // JDK 集合/工具

@Service // 业务层
@RequiredArgsConstructor // Lombok：为 final 字段生成构造器注入
public class KnowledgeServiceImpl implements KnowledgeService { // 定义类 KnowledgeServiceImpl，实现接口

    private final KnowledgeNoteMapper noteMapper; // 构造注入 noteMapper
    private final KnowledgeTagMapper tagMapper; // 构造注入 tagMapper
    private final KnowledgeNoteTagMapper noteTagMapper; // 构造注入 noteTagMapper
    private final KnowledgeReviewMapper reviewMapper; // 构造注入 reviewMapper
    private final TaskMapper taskMapper; // 构造注入 taskMapper
    private final EbbinghausReviewScheduler scheduler; // 构造注入 scheduler

    @Override // 覆盖父类/接口方法
    @Transactional // 声明式事务，异常则回滚
    public KnowledgeNote create(Long userId, KnowledgeNote note, List<String> tags) { // 方法 create
        note.setId(null); // 本行业务语句
        note.setUserId(userId); // 本行业务语句
        if (StringUtils.isBlank(note.getMediaType())) { // 条件判断
            note.setMediaType("TEXT"); // 本行业务语句
        }
        if (StringUtils.isBlank(note.getMastery())) { // 条件判断
            note.setMastery("UNKNOWN"); // 本行业务语句
        }
        if (note.getStarred() == null) { // 条件判断
            note.setStarred(0); // 本行业务语句
        }
        noteMapper.insert(note); // 插入一行
        bindTags(userId, note.getId(), tags); // 本行业务语句
        KnowledgeReview review = new KnowledgeReview(); // 赋值或调用
        review.setUserId(userId); // 本行业务语句
        review.setNoteId(note.getId()); // 本行业务语句
        review.setIntervalDays(1); // 本行业务语句
        review.setNextReviewAt(LocalDateTime.now().plusDays(1)); // 本行业务语句
        reviewMapper.insert(review); // 插入一行
        pushReviewTask(userId, note); // 本行业务语句
        return note; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    @Transactional // 声明式事务，异常则回滚
    public KnowledgeNote update(Long userId, KnowledgeNote note, List<String> tags) { // 方法 update
        KnowledgeNote old = owned(userId, note.getId()); // 赋值或调用
        note.setUserId(old.getUserId()); // 本行业务语句
        noteMapper.updateById(note); // 按主键更新
        if (tags != null) { // 条件判断
            noteTagMapper.delete(new LambdaQueryWrapper<KnowledgeNoteTag>().eq(KnowledgeNoteTag::getNoteId, note.getId())); // 类型安全查询条件
            bindTags(userId, note.getId(), tags); // 本行业务语句
        }
        return noteMapper.selectById(note.getId()); // 按主键查库
    }

    @Override // 覆盖父类/接口方法
    public void delete(Long userId, Long id) { // 方法 delete
        owned(userId, id); // 本行业务语句
        noteMapper.deleteById(id); // 本行业务语句
        noteTagMapper.delete(new LambdaQueryWrapper<KnowledgeNoteTag>().eq(KnowledgeNoteTag::getNoteId, id)); // 类型安全查询条件
    }

    @Override // 覆盖父类/接口方法
    public KnowledgeNote get(Long userId, Long id) { // 方法 get
        return owned(userId, id); // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public List<KnowledgeNote> list(Long userId, String tag, String mastery, Long taskId, Boolean starred) { // 方法 list
        List<Long> ids = null; // 赋值或调用
        if (StringUtils.isNotBlank(tag)) { // token 非空才解析
            KnowledgeTag kt = tagMapper.selectOne(new LambdaQueryWrapper<KnowledgeTag>() // 查一条
                    .eq(KnowledgeTag::getUserId, userId).eq(KnowledgeTag::getName, tag)); // 本行业务语句
            if (kt == null) { // 条件判断
                return List.of(); // 返回结果
            }
            ids = noteTagMapper.selectList(new LambdaQueryWrapper<KnowledgeNoteTag>() // 查列表
                            .eq(KnowledgeNoteTag::getTagId, kt.getId())) // 本行业务语句
                    .stream().map(KnowledgeNoteTag::getNoteId).toList(); // 本行业务语句
            if (ids.isEmpty()) { // 条件判断
                return List.of(); // 返回结果
            }
        }
        return noteMapper.selectList(new LambdaQueryWrapper<KnowledgeNote>() // 查列表
                .eq(KnowledgeNote::getUserId, userId) // 本行业务语句
                .eq(StringUtils.isNotBlank(mastery), KnowledgeNote::getMastery, mastery) // token 非空才解析
                .eq(taskId != null, KnowledgeNote::getTaskId, taskId) // 赋值或调用
                .eq(Boolean.TRUE.equals(starred), KnowledgeNote::getStarred, 1) // 本行业务语句
                .in(ids != null, KnowledgeNote::getId, ids) // 赋值或调用
                .orderByDesc(KnowledgeNote::getId)); // 本行业务语句
    }

    @Override // 覆盖父类/接口方法
    @Transactional // 声明式事务，异常则回滚
    public KnowledgeNote merge(Long userId, Long fromId, Long toId) { // 方法 merge
        KnowledgeNote from = owned(userId, fromId); // 赋值或调用
        KnowledgeNote to = owned(userId, toId); // 赋值或调用
        String content = StringUtils.defaultString(to.getContent()) + "\n" + StringUtils.defaultString(from.getContent()); // 赋值或调用
        to.setContent(content.trim()); // 本行业务语句
        noteMapper.updateById(to); // 按主键更新
        noteMapper.deleteById(fromId); // 本行业务语句
        return to; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public KnowledgeReview dueCard(Long userId) { // 方法 dueCard
        return reviewMapper.selectOne(new LambdaQueryWrapper<KnowledgeReview>() // 查一条
                .eq(KnowledgeReview::getUserId, userId) // 本行业务语句
                .le(KnowledgeReview::getNextReviewAt, LocalDateTime.now()) // 本行业务语句
                .orderByAsc(KnowledgeReview::getNextReviewAt) // 本行业务语句
                .last("limit 1")); // 本行业务语句
    }

    @Override // 覆盖父类/接口方法
    @Transactional // 声明式事务，异常则回滚
    public KnowledgeReview mark(Long userId, Long reviewId, boolean remembered) { // 方法 mark
        KnowledgeReview review = reviewMapper.selectById(reviewId); // 按主键查库
        if (review == null || !userId.equals(review.getUserId())) { // 条件判断
            throw new BizException(ErrorCode.KNOWLEDGE_NOT_FOUND); // 抛出业务或运行时异常
        }
        int next = scheduler.nextInterval(review.getIntervalDays() == null ? 1 : review.getIntervalDays(), remembered); // 赋值或调用
        review.setRemembered(remembered ? 1 : 0); // 本行业务语句
        review.setIntervalDays(next); // 本行业务语句
        review.setNextReviewAt(LocalDateTime.now().plusDays(next)); // 本行业务语句
        reviewMapper.updateById(review); // 按主键更新
        KnowledgeNote note = noteMapper.selectById(review.getNoteId()); // 按主键查库
        if (note != null) { // 条件判断
            note.setMastery(remembered ? "MASTERED" : "UNKNOWN"); // 本行业务语句
            noteMapper.updateById(note); // 按主键更新
            if (!remembered) { // 条件判断
                pushReviewTask(userId, note); // 本行业务语句
            }
        }
        return review; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public Map<String, Object> weeklySummary(Long userId, LocalDate weekStart) { // 方法 weeklySummary
        LocalDate start = weekStart == null ? LocalDate.now().minusDays(6) : weekStart; // 赋值或调用
        List<KnowledgeNote> notes = noteMapper.selectList(new LambdaQueryWrapper<KnowledgeNote>() // 查列表
                .eq(KnowledgeNote::getUserId, userId) // 本行业务语句
                .ge(KnowledgeNote::getCreatedAt, start.atStartOfDay()) // 本行业务语句
                .lt(KnowledgeNote::getCreatedAt, start.plusDays(7).atStartOfDay())); // 本行业务语句
        Map<String, List<String>> grouped = new LinkedHashMap<>(); // 赋值或调用
        for (KnowledgeNote note : notes) { // 循环
            List<KnowledgeNoteTag> rel = noteTagMapper.selectList(new LambdaQueryWrapper<KnowledgeNoteTag>() // 查列表
                    .eq(KnowledgeNoteTag::getNoteId, note.getId())); // 本行业务语句
            if (rel.isEmpty()) { // 条件判断
                grouped.computeIfAbsent("未分类", k -> new ArrayList<>()).add(note.getTitle()); // 本行业务语句
            } else { // 开始代码块
                for (KnowledgeNoteTag r : rel) { // 循环
                    KnowledgeTag tag = tagMapper.selectById(r.getTagId()); // 按主键查库
                    String name = tag == null ? "未分类" : tag.getName(); // 赋值或调用
                    grouped.computeIfAbsent(name, k -> new ArrayList<>()).add(note.getTitle()); // 本行业务语句
                }
            }
        }
        Map<String, Object> map = new LinkedHashMap<>(); // 赋值或调用
        map.put("from", start); // 本行业务语句
        map.put("count", notes.size()); // 本行业务语句
        map.put("byTag", grouped); // 本行业务语句
        return map; // 返回结果
    }

    @Override // 覆盖父类/接口方法
    public String exportText(Long userId, String tag, LocalDate from, LocalDate to) { // 方法 exportText
        List<KnowledgeNote> notes = list(userId, tag, null, null, null); // 赋值或调用
        return notes.stream() // 返回结果
                .filter(n -> from == null || n.getCreatedAt() == null || !n.getCreatedAt().toLocalDate().isBefore(from)) // 赋值或调用
                .filter(n -> to == null || n.getCreatedAt() == null || !n.getCreatedAt().toLocalDate().isAfter(to)) // 赋值或调用
                .map(n -> "# " + n.getTitle() + "\n" + StringUtils.defaultString(n.getContent())) // 本行业务语句
                .collect(Collectors.joining("\n\n")); // 本行业务语句
    }

    private void bindTags(Long userId, Long noteId, List<String> tags) { // 方法 bindTags
        if (tags == null) { // 条件判断
            return; // 本行业务语句
        }
        for (String name : tags) { // 循环
            if (StringUtils.isBlank(name)) { // 条件判断
                continue; // 本行业务语句
            }
            KnowledgeTag tag = tagMapper.selectOne(new LambdaQueryWrapper<KnowledgeTag>() // 查一条
                    .eq(KnowledgeTag::getUserId, userId).eq(KnowledgeTag::getName, name.trim())); // 本行业务语句
            if (tag == null) { // 条件判断
                tag = new KnowledgeTag(); // 赋值或调用
                tag.setUserId(userId); // 本行业务语句
                tag.setName(name.trim()); // 本行业务语句
                tagMapper.insert(tag); // 插入一行
            }
            KnowledgeNoteTag rel = new KnowledgeNoteTag(); // 赋值或调用
            rel.setNoteId(noteId); // 本行业务语句
            rel.setTagId(tag.getId()); // 本行业务语句
            noteTagMapper.insert(rel); // 插入一行
        }
    }

    private void pushReviewTask(Long userId, KnowledgeNote note) { // 方法 pushReviewTask
        Task task = new Task(); // 赋值或调用
        task.setUserId(userId); // 本行业务语句
        task.setTitle("复习：" + note.getTitle()); // 本行业务语句
        task.setContent(StringUtils.abbreviate(note.getContent(), 200)); // 本行业务语句
        task.setCategory("学习"); // 本行业务语句
        task.setPriority(0); // 本行业务语句
        task.setDurationMinutes(10); // 本行业务语句
        task.setStatus(0); // 本行业务语句
        task.setBuffered(0); // 本行业务语句
        task.setSourceType("REVIEW"); // 本行业务语句
        task.setReviewNoteId(note.getId()); // 本行业务语句
        taskMapper.insert(task); // 插入一行
    }

    private KnowledgeNote owned(Long userId, Long id) { // 方法 owned
        KnowledgeNote note = noteMapper.selectById(id); // 按主键查库
        if (note == null || !Objects.equals(userId, note.getUserId())) { // 条件判断
            throw new BizException(ErrorCode.KNOWLEDGE_NOT_FOUND); // 抛出业务或运行时异常
        }
        return note; // 返回结果
    }
}
