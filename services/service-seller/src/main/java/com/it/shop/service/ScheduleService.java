package com.it.shop.service;

import com.it.shop.bean.ProductStore;
import com.it.shop.bean.SellerAccount;
import com.it.shop.bean.SellerResponseCode;
import com.it.shop.exception.ShopApplicationException;
import com.it.shop.repository.ProductStoreRepository;
import com.it.shop.repository.SellerAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@EnableScheduling
public class ScheduleService {
    private static final Logger log = LoggerFactory.getLogger(ScheduleService.class);
    @Autowired
    private ProductStoreRepository productStoreRepository;

    @Autowired
    private SellerAccountRepository sellerAccountRepository;


    @Scheduled(cron = "0 0 22 * * ?")
    public void checkAccount() {
        double totalSelledMoney = getAllSelledMonty();
        double allSellerAccountBalance = getAllSellerAccountBalance();
        if (totalSelledMoney != allSellerAccountBalance) {
            log.error("资金结算异常...");
            throw new ShopApplicationException(SellerResponseCode.SETTLE_ERROR.getCode(), SellerResponseCode.SETTLE_ERROR.getMessage());
        }
    }

    private double getAllSelledMonty() {
        List<ProductStore> allProductStore = productStoreRepository.findAll();

        double totalSelledMoney = 0d;
        for (ProductStore productStore: allProductStore) {
            int selledQty = productStore.getInitQty() - productStore.getQtyBalance();
            double selledMoney = selledQty * productStore.getPrice();
            totalSelledMoney = totalSelledMoney + selledMoney;
        }

        return totalSelledMoney;
    }

    private double getAllSellerAccountBalance() {
        List<SellerAccount> allSellerAccounts = sellerAccountRepository.findAll();
        double totalAllSellerAccount = 0d;
        for (SellerAccount sellerAccount: allSellerAccounts) {
            double sellerAccountBalance = sellerAccount.getBalance();
            totalAllSellerAccount = totalAllSellerAccount + sellerAccountBalance;
        }

        return totalAllSellerAccount;
    }
}
