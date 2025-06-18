package com.it.shop.repository;

import com.it.shop.bean.SellerAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface SellerAccountRepository extends JpaRepository<SellerAccount, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SellerAccount> findById(Long id);
}
