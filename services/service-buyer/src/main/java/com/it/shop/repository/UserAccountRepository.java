package com.it.shop.repository;

import com.it.shop.bean.UserAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserAccount> findByUserId(Long userId);
}
