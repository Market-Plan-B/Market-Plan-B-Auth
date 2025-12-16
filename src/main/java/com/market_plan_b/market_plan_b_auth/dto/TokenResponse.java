package com.market_plan_b.market_plan_b_auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    
    public TokenResponse() {}
}