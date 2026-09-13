package com.weiji.modules.point.mapper; // MyBatis Mapper 包

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus
import com.weiji.modules.point.entity.RewardRedemption; // 本仓类 RewardRedemption
import org.apache.ibatis.annotations.Mapper; // MyBatis Mapper 标记

@Mapper // MyBatis 接口，由 MapperScan 加载
public interface RewardRedemptionMapper extends BaseMapper<RewardRedemption> { // 继承 MP CRUD
}
