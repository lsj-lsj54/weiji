package com.weiji.modules.social.mapper; // MyBatis Mapper 包

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus
import com.weiji.modules.social.entity.PlazaPost; // 本仓类 PlazaPost
import org.apache.ibatis.annotations.Mapper; // MyBatis Mapper 标记

@Mapper // MyBatis 接口，由 MapperScan 加载
public interface PlazaPostMapper extends BaseMapper<PlazaPost> { // 继承 MP CRUD
}
