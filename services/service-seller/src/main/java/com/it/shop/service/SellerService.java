package com.it.shop.service;

import com.it.shop.bean.ProductStore;
import com.it.shop.bean.SellerAccount;
import com.it.shop.bean.SellerResponseCode;
import com.it.shop.exception.ShopApplicationException;
import com.it.shop.repository.ProductStoreRepository;
import com.it.shop.repository.SellerAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
public class SellerService {

    private static final Logger log = LoggerFactory.getLogger(SellerService.class);

    public SellerService(ProductStoreRepository productStoreRepository, SellerAccountRepository sellerAccountRepository) {
        this.productStoreRepository = productStoreRepository;
        this.sellerAccountRepository = sellerAccountRepository;
    }

    private final ProductStoreRepository productStoreRepository;

    private final SellerAccountRepository sellerAccountRepository;

    @Transactional
    public ProductStore createProductStore(ProductStore productStore) {
        validateNewProductStore(productStore);
        Optional<ProductStore> productStoreOpt = productStoreRepository.findBySku(productStore.getSku());
        if (productStoreOpt.isPresent()) {
            log.info("商品sku {} 已经存在，执行加库存操作", productStore.getSku());
            return addProductStore(productStore.getSku(), productStore.getInitQty());
        } else {
            log.info("新增商品sku {}", productStore.getSku());
            productStore.setQtyBalance(productStore.getInitQty());
            productStore.setUpdatedAt(getSystemTimestamp());
            return productStoreRepository.save(productStore);
        }
    }

    @Transactional
    public ProductStore addProductStore(Long sku, Integer qty) {
        Optional<ProductStore> productStoreOpt = productStoreRepository.findBySku(sku);
        if (productStoreOpt.isPresent()) {
            log.info("给商品sku {} 添加库存 {}", sku, qty);
            ProductStore productStore = productStoreOpt.get();
            productStore.setInitQty(productStore.getInitQty() + qty);
            productStore.setQtyBalance(productStore.getQtyBalance() + qty);
            productStore.setUpdatedAt(getSystemTimestamp());
            productStoreRepository.save(productStore);
            return productStore;
        } else {
            log.info("商品sku {} 不存在", sku);
            throw new ShopApplicationException(SellerResponseCode.NO_SKU.getCode(), SellerResponseCode.NO_SKU.getMessage());
        }
    }

    @Transactional
    public Double getOrderPrice(Long sku, Integer qty) {
        Optional<ProductStore> productStoreBySkuOpt = productStoreRepository.findById(sku);
        validateStore(productStoreBySkuOpt, qty, sku);
        return productStoreBySkuOpt.get().getPrice() * qty;
    }

    @Transactional
    public void order(Long sku, Integer qty) {
        //查库存
        Optional<ProductStore> productStoreBySkuOpt = productStoreRepository.findBySku(sku);
        validateStore(productStoreBySkuOpt, qty, sku);
        double totalCost = productStoreBySkuOpt.get().getPrice() * qty;

        //扣库存
        ProductStore productStore = productStoreBySkuOpt.get();
        productStore.setQtyBalance(productStore.getQtyBalance() - qty);
        productStore.setUpdatedAt(getSystemTimestamp());
        productStoreRepository.save(productStore);

        //给账户加钱, 账户要预先创建
        List<SellerAccount> sellerAccounts = sellerAccountRepository.findAll();
        validateSellerAccount(sellerAccounts);
        addMoneyToAccount(sellerAccounts.get(0), totalCost);

    }

    private void validateNewProductStore(ProductStore productStore) {
        if (productStore.getPrice() <= 0) {
            throw new ShopApplicationException(SellerResponseCode.PRICE_INVALID.getCode(), SellerResponseCode.PRICE_INVALID.getMessage());
        }
        if (productStore.getInitQty() <= 0) {
            throw new ShopApplicationException(SellerResponseCode.QTY_INVALID.getCode(), SellerResponseCode.QTY_INVALID.getMessage());
        }
    }

    private void validateStore(Optional<ProductStore> productStoreBySkuOpt, Integer qty, Long sku) {
        if (productStoreBySkuOpt.isPresent()) {
            if (productStoreBySkuOpt.get().getQtyBalance() < qty) {
                log.error("商品sku {} 库存不足", sku);
                throw new ShopApplicationException(SellerResponseCode.NOT_ENOUGH_STORE.getCode(), SellerResponseCode.NOT_ENOUGH_STORE.getMessage());
            }
        } else {
            log.info("商品sku {} 不存在", sku);
            throw new ShopApplicationException(SellerResponseCode.NO_SKU.getCode(), SellerResponseCode.NO_SKU.getMessage());
        }
    }

    private void validateSellerAccount(List<SellerAccount> sellerAccounts) {
        if (sellerAccounts == null || sellerAccounts.isEmpty()) {
            log.error("账户不存在");
            throw new ShopApplicationException(SellerResponseCode.ACCOUNT_NOT_EXIST.getCode(), SellerResponseCode.ACCOUNT_NOT_EXIST.getMessage());
        }
    }

    private void addMoneyToAccount(SellerAccount sellerAccount, double totalCost) {
        Optional<SellerAccount> lockedAccountOpt = sellerAccountRepository.findById(sellerAccount.getId());
        SellerAccount lockedAccount = lockedAccountOpt.get();
        lockedAccount.setBalance(lockedAccount.getBalance() + totalCost);
        lockedAccount.setUpdatedAt(getSystemTimestamp());
        sellerAccountRepository.save(lockedAccount);
    }

    private Timestamp getSystemTimestamp() {
        ZoneId zone = ZoneId.systemDefault();
        long timestamp = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        return new Timestamp(timestamp);
    }
}
