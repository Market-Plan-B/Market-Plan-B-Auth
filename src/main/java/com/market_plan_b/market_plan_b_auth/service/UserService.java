package com.market_plan_b.market_plan_b_auth.service;

import com.market_plan_b.market_plan_b_auth.dto.JwtToken;

public interface UserService {
    JwtToken signIn(String email, String password);
    JwtToken refreshToken(String refreshToken);
    void logout(String refreshToken);
}
