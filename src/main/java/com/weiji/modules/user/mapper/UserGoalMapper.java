package com.weiji.modules.user.mapper; // MyBatis Mapper 包

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus
import com.weiji.modules.user.entity.UserGoal; // 本仓类 UserGoal
import org.apache.ibatis.annotations.Mapper; // MyBatis Mapper 标记

@Mapper // MyBatis 接口，由 MapperScan 加载
public interface UserGoalMapper extends BaseMapper<UserGoal> { // 继承 MP CRUD
}
