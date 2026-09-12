package com.weiji.modules.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(max = 64, message = "昵称过长")
    private String nickname;

    @Size(max = 512, message = "头像地址过长")
    private String avatar;
}
