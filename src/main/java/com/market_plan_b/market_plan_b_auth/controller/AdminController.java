package com.market_plan_b.market_plan_b_auth.controller;

import com.market_plan_b.market_plan_b_auth.domain.Role;
import com.market_plan_b.market_plan_b_auth.domain.User;
import com.market_plan_b.market_plan_b_auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/create-admin")
    public String createAdmin() {
        String encodedPassword = passwordEncoder.encode("admin1234");
        
        User admin = User.builder()
                .name("관리자")
                .email("admin")
                .password(encodedPassword)
                .role(Role.ADMIN)
                .build();
        
        userRepository.save(admin);
        
        return "관리자 계정 생성 완료 - 이메일: admin, 비밀번호: admin1234";
    }
}