package com.it.shop.controller;

import com.it.shop.bean.*;
import com.it.shop.exception.ShopApplicationException;
import com.it.shop.service.BuyerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping("/buyer")
@Tag(name = "User Controller", description = "User operations to maintain User and Order.")
public class BuyerController {

    @Autowired
    BuyerService buyerService;

    @PostMapping("/create")
    @Operation(summary = "创建新用户", description = "创建新用户.")
    public RestResponse<User> createUser(@RequestBody User user) {
        StatusCode statusCode = createSuccessStatusCode();
        try {
            User createdUser = buyerService.createUser(user);
            return buildResponse(createdUser, statusCode);
        } catch (ShopApplicationException e) {
            statusCode = e.getStatusCode();
        }

        return buildResponse(user, statusCode);
    }

    @PutMapping("/addMoney/{userId}/{money}")
    @Operation(summary = "给用户充值", description = "给用户充值.")
    public RestResponse<UserAccount> addMoney(@PathVariable Long userId, @PathVariable Double money) {
        StatusCode statusCode = createSuccessStatusCode();
        try {
            UserAccount userAccount = buyerService.addMoney(userId, money);
            return buildResponse(userAccount, statusCode);
        } catch (ShopApplicationException e) {
            statusCode = e.getStatusCode();
        }
        return buildResponse(Optional.empty(), statusCode);
    }

    @PutMapping("/order/{userId}/{sku}/{qty}")
    @Operation(summary = "用户下单购买某个商品", description = "用户下单购买某个商品.")
    public RestResponse<UserOrder> order(@PathVariable Long userId, @PathVariable Long sku, @PathVariable Integer qty) {
        StatusCode statusCode = createSuccessStatusCode();
        try {
            UserOrder userOrder = buyerService.order(userId, sku, qty);
            return buildResponse(userOrder, statusCode);
        } catch (ShopApplicationException e) {
            statusCode = e.getStatusCode();
        }
        return buildResponse(Optional.empty(), statusCode);
    }

    private StatusCode createSuccessStatusCode() {
        return StatusCode.create(BuyerResponseCode.SUCCESS.getCode(), BuyerResponseCode.SUCCESS.getMessage());
    }

    private RestResponse buildResponse(Object data, StatusCode statusCode) {
        RestResponse restResponse = RestResponse.create().buildStatus(statusCode);
        restResponse.setData(data);
        return restResponse;
    }
}
