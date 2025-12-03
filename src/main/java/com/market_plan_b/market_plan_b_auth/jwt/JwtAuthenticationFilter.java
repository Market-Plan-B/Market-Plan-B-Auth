package com.market_plan_b.market_plan_b_auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String accessToken = resolveToken(request);
        String refreshToken = resolveRefreshToken(request);

        if (StringUtils.hasText(accessToken)) {
            if (jwtTokenProvider.validateToken(accessToken)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else if (StringUtils.hasText(refreshToken)) {
                try {
                    // Access Token이 만료되었지만 Refresh Token이 유효한 경우 새 토큰 발급
                    com.market_plan_b.market_plan_b_auth.dto.JwtToken newToken = jwtTokenProvider.refreshAccessToken(refreshToken);
                    
                    // 새 토큰을 응답 헤더에 추가
                    response.setHeader("New-Access-Token", newToken.getAccessToken());
                    response.setHeader("New-Refresh-Token", newToken.getRefreshToken());
                    
                    // 새 Access Token으로 인증 설정
                    Authentication authentication = jwtTokenProvider.getAuthentication(newToken.getAccessToken());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (Exception e) {
                    // Refresh Token도 유효하지 않은 경우 인증 실패
                    log.warn("토큰 재발급 실패: {}", e.getMessage());
                    response.setHeader("Token-Error", "REFRESH_FAILED");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    private String resolveRefreshToken(HttpServletRequest request) {
        return request.getHeader("Refresh-Token");
    }
}