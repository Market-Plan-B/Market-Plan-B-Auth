package com.market_plan_b.market_plan_b_auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
public class JwtToken {
    private String grantType;
    private String accessToken;
    private String refreshToken;
    private LocalDateTime refreshTokenExpire;
    private String userName;
    private Integer userId;
    
    public JwtToken() {}
}
