package com.market_plan_b.market_plan_b_auth.repository;

import com.market_plan_b.market_plan_b_auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByEmail(String email);
    Optional<User> findByRefreshToken(String refreshToken);
}
