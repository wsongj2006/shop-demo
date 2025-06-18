package com.it.shop.repository;

import com.it.shop.bean.ProductStore;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface ProductStoreRepository extends JpaRepository<ProductStore, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ProductStore> findBySku(Long sku);
}
