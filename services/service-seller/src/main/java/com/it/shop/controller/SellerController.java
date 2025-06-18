package com.it.shop.controller;

import com.it.shop.bean.ProductStore;
import com.it.shop.bean.RestResponse;
import com.it.shop.bean.SellerResponseCode;
import com.it.shop.bean.StatusCode;
import com.it.shop.exception.ShopApplicationException;
import com.it.shop.service.ScheduleService;
import com.it.shop.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/seller")
@Tag(name = "Seller Controller", description = "Seller operations to maintain Product Store and Account.")
public class SellerController {

    @Autowired
    private SellerService sellerService;

    @Autowired
    private ScheduleService scheduleService;

    @PostMapping("/createProduct")
    @Operation(summary = "创建一个新的商品，提供库存，单价", description = "创建一个新的商品，提供库存，单价")
    public RestResponse<ProductStore> createProductStore(ProductStore productStore) {
        StatusCode statusCode = createSuccessStatusCode();
        try {
            ProductStore createdProductStore = sellerService.createProductStore(productStore);
            return buildResponse(createdProductStore, statusCode);
        } catch (ShopApplicationException e) {
            statusCode = e.getStatusCode();
        }
        return buildResponse(Optional.empty(), statusCode);
    }

    @PutMapping("/addStore/{sku}/{qty}")
    @Operation(summary = "给已有商品添加库存", description = "给已有商品添加库存")
    public RestResponse<ProductStore> addProductStore(@PathVariable Long sku, @PathVariable Integer qty) {
        StatusCode statusCode = createSuccessStatusCode();
        try {
            ProductStore updatedProductStore = sellerService.addProductStore(sku, qty);
            return buildResponse(updatedProductStore, statusCode);
        } catch (ShopApplicationException e) {
            statusCode = e.getStatusCode();
        }
        return buildResponse(Optional.empty(), statusCode);
    }

    @GetMapping("/getOrderPrice/{sku}/{qty}")
    @Operation(summary = "获取将要下单的总价", description = "获取将要下单的总价")
    public RestResponse<Double> getOrderPrice(@PathVariable Long sku, @PathVariable Integer qty) {
        StatusCode statusCode = createSuccessStatusCode();
        try {
            Double orderPrice = sellerService.getOrderPrice(sku, qty);
            return buildResponse(orderPrice, statusCode);
        } catch (ShopApplicationException e) {
            statusCode = e.getStatusCode();
        }
        return buildResponse(Optional.empty(), statusCode);
    }

    @PutMapping("/order/{sku}/{qty}")
    @Operation(summary = "下单", description = "下单.")
    public RestResponse<String> order(@PathVariable Long sku, @PathVariable Integer qty) {
        StatusCode statusCode = createSuccessStatusCode();
        try {
            sellerService.order(sku, qty);
            return buildResponse("SUCCESS", statusCode);
        } catch (ShopApplicationException e) {
            statusCode = e.getStatusCode();
        }
        return buildResponse(Optional.empty(), statusCode);
    }

    @GetMapping("/check/account")
    @Operation(summary = "检查账户余额是否有异常", description = "检查账户余额是否有异常.")
    public RestResponse<String> checkAccount() {
        StatusCode statusCode = createSuccessStatusCode();
        try {
            scheduleService.checkAccount();
            return buildResponse("SUCCESS", statusCode);
        } catch (ShopApplicationException e) {
            statusCode = e.getStatusCode();
        }
        return buildResponse(Optional.empty(), statusCode);
    }

    private StatusCode createSuccessStatusCode() {
        return StatusCode.create(SellerResponseCode.SUCCESS.getCode(), SellerResponseCode.SUCCESS.getMessage());
    }

    private RestResponse buildResponse(Object data, StatusCode statusCode) {
        RestResponse restResponse = RestResponse.create().buildStatus(statusCode);
        restResponse.setData(data);
        return restResponse;
    }
}
