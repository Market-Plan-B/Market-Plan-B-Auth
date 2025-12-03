package com.market_plan_b.market_plan_b_auth.controller;

import com.market_plan_b.market_plan_b_auth.dto.JwtToken;
import com.market_plan_b.market_plan_b_auth.dto.RefreshTokenDto;
import com.market_plan_b.market_plan_b_auth.dto.SignDto;
import com.market_plan_b.market_plan_b_auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/sign-in")
    public ResponseEntity<JwtToken> signIn(@RequestBody SignDto signDto) {
        String email = signDto.getEmail();
        String password = signDto.getPassword();

        JwtToken token = userService.signIn(email, password);

        log.info("request email = {}, password = {}", email, password);
        log.info("jwtToken accessToken = {}, refreshToken = {}", token.getAccessToken(), token.getRefreshToken());

        return ResponseEntity.ok(token);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody RefreshTokenDto refreshTokenDto) {
        try {
            userService.logout(refreshTokenDto.getRefreshToken());
            return ResponseEntity.ok("로그아웃 성공");
        } catch (Exception e) {
            log.warn("로그아웃 시 예외 발생: {}", e.getMessage());
            return ResponseEntity.ok("로그아웃 완료"); 
        }
    }
}