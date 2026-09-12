package com.weiji.modules.user.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenVO {

    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private String tokenType;
}
