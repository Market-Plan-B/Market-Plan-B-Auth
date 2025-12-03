package com.market_plan_b.market_plan_b_auth.service;

import com.market_plan_b.market_plan_b_auth.domain.Role;
import com.market_plan_b.market_plan_b_auth.domain.User;
import com.market_plan_b.market_plan_b_auth.dto.JwtToken;
import com.market_plan_b.market_plan_b_auth.jwt.JwtTokenProvider;
import com.market_plan_b.market_plan_b_auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    @Override
    public JwtToken signIn(String email, String password) {
        log.info("로그인 시도 - 이메일: {}, 비밀번호 길이: {}", email, password.length());
        
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, password);
        log.info("인증 토큰 생성 완료");

        try {
            Authentication authentication =
                    authenticationManager.authenticate(authenticationToken);
            log.info("인증 성공: {}", authentication.getName());
            
            JwtToken token = jwtTokenProvider.generateToken(authentication);
            
            // Refresh Token 및 만료시간 DB에 저장 및 사용자 이름 추가
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            
            log.info("DB에서 조회된 사용자 - ID: {}, 이름: {}, 이메일: {}, 역할: {}", 
                    user.getId(), user.getName(), user.getEmail(), user.getRole());
            
            user.setRefreshToken(token.getRefreshToken());
            user.setRefreshTokenExpire(token.getRefreshTokenExpire());
            userRepository.save(user);
            
            // 사용자 이름을 토큰에 추가
            token.setUserName(user.getName());
            log.info("토큰에 사용자 이름 추가: {}", user.getName());
            
            log.info("JWT 토큰 생성 완룄 - userName: {}", token.getUserName());
            return token;
        } catch (Exception e) {
            log.error("인증 실패: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    @Override
    public JwtToken refreshToken(String refreshToken) {
        // Refresh Token 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("유효하지 않은 Refresh Token입니다.");
        }

        // DB에서 Refresh Token으로 사용자 찾기
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 만료시간 확인
        if (user.getRefreshTokenExpire().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("만료된 Refresh Token입니다.");
        }

        // 새로운 Access Token과 Refresh Token 모두 생성
        Authentication authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                user.getEmail(), "", 
                java.util.Collections.singletonList(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                )
        );
        
        JwtToken newToken = jwtTokenProvider.generateToken(authentication);
        
        // 새로운 Refresh Token DB에 저장
        user.setRefreshToken(newToken.getRefreshToken());
        user.setRefreshTokenExpire(newToken.getRefreshTokenExpire());
        userRepository.save(user);
        
        return newToken;
    }

    @Transactional
    @Override
    public void logout(String refreshToken) {
        // Refresh Token 유효성 검증
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new RuntimeException("유효하지 않은 Refresh Token입니다.");
        }

        // DB에서 Refresh Token으로 사용자 찾기
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElse(null);

        // 사용자가 없거나 이미 로그아웃된 경우
        if (user == null) {
            log.warn("이미 로그아웃되었거나 유효하지 않은 Refresh Token: {}", refreshToken.substring(0, Math.min(20, refreshToken.length())));
            return; // 에러 대신 정상 처리
        }

        // Refresh Token 삭제 전 로그
        log.info("로그아웃 시작 - 사용자: {}, 기존 토큰: {}", user.getEmail(), user.getRefreshToken() != null ? "EXISTS" : "NULL");
        
        // Refresh Token 삭제
        user.setRefreshToken(null);
        user.setRefreshTokenExpire(null);
        User savedUser = userRepository.save(user);
        
        // 삭제 후 확인
        log.info("로그아웃 완료 - 사용자: {}, 토큰 삭제 후: {}", savedUser.getEmail(), savedUser.getRefreshToken() != null ? "EXISTS" : "NULL");
    }
}