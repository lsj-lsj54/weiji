package com.weiji.modules.knowledge.mapper; // MyBatis Mapper 包

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus
import com.weiji.modules.knowledge.entity.KnowledgeNote; // 本仓类 KnowledgeNote
import org.apache.ibatis.annotations.Mapper; // MyBatis Mapper 标记

@Mapper // MyBatis 接口，由 MapperScan 加载
public interface KnowledgeNoteMapper extends BaseMapper<KnowledgeNote> { // 继承 MP CRUD
}
