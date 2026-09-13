package com.weiji.modules.user.mapper; // MyBatis Mapper 包

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus
import com.weiji.modules.user.entity.User; // 本仓类 User
import org.apache.ibatis.annotations.Mapper; // MyBatis Mapper 标记

@Mapper // MyBatis 接口，由 MapperScan 加载
public interface UserMapper extends BaseMapper<User> { // 继承 MP CRUD
}
