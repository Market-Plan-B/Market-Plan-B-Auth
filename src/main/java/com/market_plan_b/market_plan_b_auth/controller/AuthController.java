package com.market_plan_b.market_plan_b_auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.market_plan_b.market_plan_b_auth.dto.AuthResponse;
import com.market_plan_b.market_plan_b_auth.dto.JwtToken;
import com.market_plan_b.market_plan_b_auth.dto.SignDto;
import com.market_plan_b.market_plan_b_auth.dto.TokenResponse;
import com.market_plan_b.market_plan_b_auth.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponse> signIn(@RequestBody SignDto signDto, HttpServletResponse response) {
        String email = signDto.getEmail();
        String password = signDto.getPassword();

        JwtToken token = userService.signIn(email, password);

        // Refresh Token을 HttpOnly 쿠키로 설정
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", token.getRefreshToken())
                .httpOnly(true)
                .secure(true) // HTTPS에서는 true로 설정
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7일
                .build();
        
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        log.info("request email = {}, password = {}", email, password);
        log.info("jwtToken accessToken = {}, refreshToken = {}", token.getAccessToken(), token.getRefreshToken());

        // 클라이언트 응답용 DTO 생성 
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(token.getAccessToken())
                .userName(token.getUserName())
                .userId(token.getUserId())
                .build();
        
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response) {
        try {
            JwtToken newToken = userService.refreshToken(refreshToken);
            
            // 새로운 Refresh Token을 쿠키로 설정
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", newToken.getRefreshToken())
                    .httpOnly(true)
                    .secure(true) 
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60) // 7일
                    .build();
            
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
            
            // 토큰 재발급용 응답
            TokenResponse tokenResponse = TokenResponse.builder()
                    .accessToken(newToken.getAccessToken())
                    .build();
            
            log.info("토큰 갱신 성공 - 새로운 accessToken 생성");
            return ResponseEntity.ok(tokenResponse);
        } catch (Exception e) {
            log.error("토큰 갱신 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response) {
        try {
            userService.logout(refreshToken);
            
            // 쿠키 삭제
            ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(0)
                    .build();
            
            response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
            
            return ResponseEntity.ok("로그아웃 성공");
        } catch (Exception e) {
            log.warn("로그아웃 시 예외 발생: {}", e.getMessage());
            return ResponseEntity.ok("로그아웃 완료"); 
        }
    }
}