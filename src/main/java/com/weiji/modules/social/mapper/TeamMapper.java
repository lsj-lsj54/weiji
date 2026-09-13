package com.weiji.modules.social.mapper; // MyBatis Mapper 包

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus
import com.weiji.modules.social.entity.Team; // 本仓类 Team
import org.apache.ibatis.annotations.Mapper; // MyBatis Mapper 标记

@Mapper // MyBatis 接口，由 MapperScan 加载
public interface TeamMapper extends BaseMapper<Team> { // 继承 MP CRUD
}
