package com.weiji.modules.point.mapper; // MyBatis Mapper 包

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus
import com.weiji.modules.point.entity.Reward; // 本仓类 Reward
import org.apache.ibatis.annotations.Mapper; // MyBatis Mapper 标记

@Mapper // MyBatis 接口，由 MapperScan 加载
public interface RewardMapper extends BaseMapper<Reward> { // 继承 MP CRUD
}
