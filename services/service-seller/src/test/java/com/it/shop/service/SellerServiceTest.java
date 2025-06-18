package com.it.shop.service;

import com.it.shop.bean.ProductStore;
import com.it.shop.bean.SellerAccount;
import com.it.shop.repository.ProductStoreRepository;
import com.it.shop.repository.SellerAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SellerServiceTest {

    private SellerService sellerService;

    @Mock
    private ProductStoreRepository productStoreRepository;
    @Mock
    private SellerAccountRepository sellerAccountRepository;

    @BeforeEach
    public void before() {
        sellerService = new SellerService(productStoreRepository, sellerAccountRepository);
    }

    //测试新增商品
    @Test
    public void testCreateProductStore() {

        when(productStoreRepository.findBySku(any())).thenReturn(Optional.empty());

        ProductStore productStore = new ProductStore();
        productStore.setInitQty(2000);
        productStore.setSku(100l);
        productStore.setPrice(5.1d);

        when(productStoreRepository.save(any())).thenReturn(productStore);
        sellerService.createProductStore(productStore);
        Assert.isTrue(productStore.getQtyBalance() == 2000, "");
    }

    //测试给已有商品新增库存
    @Test
    public void testAddProductStore() {

        ProductStore productStore = new ProductStore();
        productStore.setInitQty(2000);
        productStore.setQtyBalance(1000);
        productStore.setSku(100l);
        productStore.setPrice(5.1d);
        Optional<ProductStore> productStoreOpt = Optional.of(productStore);

        when(productStoreRepository.findBySku(any())).thenReturn(productStoreOpt);
        when(productStoreRepository.save(any())).thenReturn(productStore);

        ProductStore newProductStore = new ProductStore();
        newProductStore.setInitQty(500);
        newProductStore.setSku(100l);
        newProductStore.setPrice(5.1d);
        sellerService.createProductStore(newProductStore);
        Assert.isTrue(productStore.getInitQty() == 2500, "");
        Assert.isTrue(productStore.getQtyBalance() == 1500, "");
    }

    @Test
    //测试获取订单总价
    public void testOrderPrice() {
        Long sku = 100l;
        int qty = 20;
        ProductStore productStore = new ProductStore();
        productStore.setInitQty(2000);
        productStore.setQtyBalance(1000);
        productStore.setSku(sku);
        productStore.setPrice(3d);
        Optional<ProductStore> productStoreOpt = Optional.of(productStore);

        when(productStoreRepository.findById(any())).thenReturn(productStoreOpt);

        Double orderPrice = sellerService.getOrderPrice(sku, qty);
        Assert.isTrue(orderPrice == qty * 3d, "");
    }

    //测试下单扣库存和账户余额
    @Test
    public void testOrder() {
        Long sku = 100l;
        int qty = 20;

        ProductStore productStore = new ProductStore();
        productStore.setInitQty(2000);
        productStore.setQtyBalance(1000);
        productStore.setSku(sku);
        productStore.setPrice(5d);
        Optional<ProductStore> productStoreOpt = Optional.of(productStore);

        when(productStoreRepository.findBySku(any())).thenReturn(productStoreOpt);
        when(productStoreRepository.save(any())).thenReturn(productStore);

        SellerAccount sellerAccount = new SellerAccount();
        sellerAccount.setId(20l);
        sellerAccount.setBalance(200d);
        Optional<SellerAccount> sellerAccountOpt = Optional.of(sellerAccount);
        List<SellerAccount> sellerAccountList = new ArrayList<>();
        sellerAccountList.add(sellerAccount);
        when(sellerAccountRepository.findAll()).thenReturn(sellerAccountList);
        when(sellerAccountRepository.findById(any())).thenReturn(sellerAccountOpt);
        when(sellerAccountRepository.save(any())).thenReturn(sellerAccount);

        sellerService.order(sku, qty);
        Assert.isTrue(productStore.getQtyBalance() == 980, "");
        Assert.isTrue(sellerAccount.getBalance() == 300, "");
    }
}
