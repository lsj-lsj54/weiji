package com.weiji.modules.task.mapper; // MyBatis Mapper 包

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus
import com.weiji.modules.task.entity.FocusSession; // 本仓类 FocusSession
import org.apache.ibatis.annotations.Mapper; // MyBatis Mapper 标记

@Mapper // MyBatis 接口，由 MapperScan 加载
public interface FocusSessionMapper extends BaseMapper<FocusSession> { // 继承 MP CRUD
}
