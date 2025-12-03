// package com.market_plan_b.market_plan_b_auth.controller;

// import com.market_plan_b.market_plan_b_auth.domain.Role;
// import com.market_plan_b.market_plan_b_auth.domain.User;
// import com.market_plan_b.market_plan_b_auth.repository.UserRepository;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// @Slf4j
// @RestController
// @RequestMapping("/api/setup")
// @RequiredArgsConstructor
// public class SetupController {

//     private final UserRepository userRepository;
//     private final PasswordEncoder passwordEncoder;

//     @GetMapping("/create-simple-users")
//     public ResponseEntity<String> createSimpleUsers() {
//         try {
//             String password = "password123";
//             String encodedPassword = passwordEncoder.encode(password);

//             // 일반 사용자 생성
//             if (userRepository.findByEmail("user100@example.com").isEmpty()) {
//                 User user = new User();
//                 user.setRole(Role.USER);
//                 user.setName("신동연");
//                 user.setEmail("user100@example.com");
//                 user.setPassword(encodedPassword);
//                 userRepository.save(user);
//                 log.info("일반 사용자 생성 완료: user@example.com");
//             }

//             // 관리자 생성
//             if (userRepository.findByEmail("admin100@example.com").isEmpty()) {
//                 User admin = new User();
//                 admin.setRole(Role.ADMIN);
//                 admin.setName("관리자100");
//                 admin.setEmail("admin100@example.com");
//                 admin.setPassword(encodedPassword);
//                 userRepository.save(admin);
//                 log.info("관리자 생성 완료: admin@example.com");
//             }

//             return ResponseEntity.ok("사용자 생성 완료!\n\n" +
//                     "일반 사용자: user@example.com / password123\n" +
//                     "관리자: admin@example.com / password123");

//         } catch (Exception e) {
//             log.error("사용자 생성 실패: {}", e.getMessage());
//             return ResponseEntity.badRequest().body("사용자 생성 실패: " + e.getMessage());
//         }
//     }
// }