package com.market_plan_b.market_plan_b_auth.auth;

import com.market_plan_b.market_plan_b_auth.domain.User;
import com.market_plan_b.market_plan_b_auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("로그인 시도: {}", email);
        
        return userRepository.findByEmail(email)
                .map(user -> {
                    log.info("사용자 찾음: {}, 비밀번호: {}", user.getEmail(), user.getPassword());
                    return createUserDetails(user);
                })
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없음: {}", email);
                    return new UsernameNotFoundException("해당하는 유저를 찾을 수 없습니다: " + email);
                });
    }

    private UserDetails createUserDetails(User user) {

        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
        log.info("권한 설정: {}", authority.getAuthority());

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(authority)
                .build();
        
        log.info("UserDetails 생성 완료 - username: {}, password 시작: {}", 
                userDetails.getUsername(), 
                userDetails.getPassword().substring(0, Math.min(20, userDetails.getPassword().length())));
        
        return userDetails;
    }
}