package com.weiji.modules.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weiji.modules.knowledge.entity.KnowledgeNote;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface KnowledgeNoteMapper extends BaseMapper<KnowledgeNote> {
}
