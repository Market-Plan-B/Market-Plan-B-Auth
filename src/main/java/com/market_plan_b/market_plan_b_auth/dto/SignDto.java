package com.market_plan_b.market_plan_b_auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class SignDto {
    private String email;
    private String password;
}
