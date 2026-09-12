package com.weiji.modules.user.service;

import com.weiji.modules.user.dto.UpdateProfileRequest;
import com.weiji.modules.user.vo.UserVO;

public interface UserService {

    UserVO currentUser();

    UserVO updateProfile(UpdateProfileRequest request);
}
