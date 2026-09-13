package com.weiji.modules.user.mapper; // MyBatis Mapper 包

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus
import com.weiji.modules.user.entity.UserPreference; // 本仓类 UserPreference
import org.apache.ibatis.annotations.Mapper; // MyBatis Mapper 标记

@Mapper // MyBatis 接口，由 MapperScan 加载
public interface UserPreferenceMapper extends BaseMapper<UserPreference> { // 继承 MP CRUD
}
