package com.weiji.modules.point.mapper; // MyBatis Mapper 包

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus
import com.weiji.modules.point.entity.UserBadge; // 本仓类 UserBadge
import org.apache.ibatis.annotations.Mapper; // MyBatis Mapper 标记

@Mapper // MyBatis 接口，由 MapperScan 加载
public interface UserBadgeMapper extends BaseMapper<UserBadge> { // 继承 MP CRUD
}
