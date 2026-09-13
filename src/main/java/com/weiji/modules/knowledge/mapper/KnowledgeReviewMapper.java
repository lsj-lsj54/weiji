package com.weiji.modules.knowledge.mapper; // MyBatis Mapper 包

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus
import com.weiji.modules.knowledge.entity.KnowledgeReview; // 本仓类 KnowledgeReview
import org.apache.ibatis.annotations.Mapper; // MyBatis Mapper 标记

@Mapper // MyBatis 接口，由 MapperScan 加载
public interface KnowledgeReviewMapper extends BaseMapper<KnowledgeReview> { // 继承 MP CRUD
}
