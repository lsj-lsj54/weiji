package com.weiji.modules.user.service.impl;

import com.weiji.common.enums.ErrorCode;
import com.weiji.common.exception.BizException;
import com.weiji.framework.security.UserContext;
import com.weiji.modules.user.dto.UpdateProfileRequest;
import com.weiji.modules.user.entity.User;
import com.weiji.modules.user.mapper.UserMapper;
import com.weiji.modules.user.service.UserService;
import com.weiji.modules.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public UserVO currentUser() {
        return toVo(loadCurrent());
    }

    @Override
    public UserVO updateProfile(UpdateProfileRequest request) {
        User user = loadCurrent();
        if (StringUtils.isNotBlank(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (StringUtils.isNotBlank(request.getAvatar())) {
            user.setAvatar(request.getAvatar());
        }
        userMapper.updateById(user);
        return toVo(userMapper.selectById(user.getId()));
    }

    private User loadCurrent() {
        Long userId = UserContext.userId();
        if (userId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private UserVO toVo(User user) {
        return UserVO.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .build();
    }
}
