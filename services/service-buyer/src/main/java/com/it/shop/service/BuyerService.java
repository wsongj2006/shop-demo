package com.it.shop.service;

import com.it.shop.bean.*;
import com.it.shop.exception.ShopApplicationException;
import com.it.shop.repository.UserAccountRepository;
import com.it.shop.repository.UserOrderRepository;
import com.it.shop.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
public class BuyerService {

    private static final Logger log = LoggerFactory.getLogger(BuyerService.class);

    @Autowired
    public BuyerService(UserRepository userRepository, UserAccountRepository userAccountRepository,
                        UserOrderRepository userOrderRepository,
                        RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.userAccountRepository = userAccountRepository;
        this.userOrderRepository = userOrderRepository;
        this.restTemplate = restTemplate;
    }

    private final UserRepository userRepository;

    private final UserAccountRepository userAccountRepository;

    private final UserOrderRepository userOrderRepository;

    private final RestTemplate restTemplate;

    public User createUser(User user) {
        validateNewUser(user);
        user.setUpdatedAt(getSystemTimestamp());
        return userRepository.save(user);
    }

    @Transactional
    public UserAccount addMoney(Long userId, double money) {
        validateMoney(money);
        validateUserExist(userId);

        Optional<UserAccount> existingAccountOpt = userAccountRepository.findByUserId(userId);
        if (existingAccountOpt.isPresent()) {
            log.info("为用户{}充值{}", userId, money);
            return addMoneyToExistingUser(existingAccountOpt.get(), money);
        } else {
            log.info("给新用户{}充值{}", userId, money);
            return createNewUserAccount(userId, money);
        }
    }

    @Transactional
    public UserOrder order(Long userId, Long sku, Integer qty) {
        validateUserExist(userId);
        validateQty(qty);

        //查询总价
        Double cost = getTotalCost(sku, qty);
        validateOrderCost(cost);

        //扣款
        Optional<UserAccount> userAccountOpt = userAccountRepository.findByUserId(userId);
        validateBalance(userAccountOpt, cost);//验证余额
        UserAccount userAccount = userAccountOpt.get();
        userAccount.setBalance(userAccount.getBalance() - cost);
        userAccountRepository.save(userAccount);

        //创建订单
        UserOrder userOrder = new UserOrder();
        userOrder.setUserId(userId);
        userOrder.setQty(qty);
        userOrder.setSku(sku);
        userOrder.setUpdatedAt(getSystemTimestamp());
        userOrderRepository.save(userOrder);

        //扣库存
        sendOrder(sku, qty);

        return userOrder;
    }

    private void validateNewUser(User user) {
        if (!StringUtils.hasText(user.getUserName())) {
            throw new ShopApplicationException(BuyerResponseCode.USER_NAME_REQUIRED.getCode(), BuyerResponseCode.USER_NAME_REQUIRED.getMessage());
        }
    }

    private void validateUserExist(Long userId) {
        if (!IfUserExisting(userId)) {
            log.error("用户ID {} 不存在", userId);
            throw new ShopApplicationException(BuyerResponseCode.USER_NOT_EXIST.getCode(), BuyerResponseCode.USER_NOT_EXIST.getMessage());
        }
    }

    private void validateMoney(Double money) {
        if (money <= 0 || money > 10000) {
            log.error("充值金额非法 {}", money);
            throw new ShopApplicationException(BuyerResponseCode.MONEY_IN_VALIDE.getCode(), BuyerResponseCode.MONEY_IN_VALIDE.getMessage());
        }
    }

    private void validateBalance(Optional<UserAccount> userAccountOpt, Double totalCost) {
        if (userAccountOpt.isEmpty() || userAccountOpt.get().getBalance() < totalCost) {
            log.error("余额不足");
            throw new ShopApplicationException(BuyerResponseCode.BALANCE_IN_VALID.getCode(), BuyerResponseCode.BALANCE_IN_VALID.getMessage());
        }
    }

    private void validateOrderCost(Double cost) {
        if (cost == null) {
            log.error("订单价格获取失败");
            throw new ShopApplicationException(BuyerResponseCode.PRICE_QUERY_FAILED.getCode(), BuyerResponseCode.PRICE_QUERY_FAILED.getMessage());
        }
    }

    private void validateQty(Integer qty) {
        if (qty <= 0) {
            log.error("商品数量非法值");
            throw new ShopApplicationException(BuyerResponseCode.QTY_IN_VALID.getCode(), BuyerResponseCode.QTY_IN_VALID.getMessage());
        }
    }

    private boolean IfUserExisting(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        return userOpt.isPresent();
    }

    private UserAccount createNewUserAccount(Long userId, Double money) {
        UserAccount newUserAccount = new UserAccount();
        newUserAccount.setBalance(money);
        newUserAccount.setUserId(userId);
        newUserAccount.setUpdatedAt(getSystemTimestamp());
        userAccountRepository.save(newUserAccount);
        return newUserAccount;
    }

    private UserAccount addMoneyToExistingUser(UserAccount existingAccount, Double money) {
        existingAccount.setBalance(existingAccount.getBalance() + money);
        userAccountRepository.save(existingAccount);
        return existingAccount;
    }

    private Timestamp getSystemTimestamp() {
        ZoneId zone = ZoneId.systemDefault();
        long timestamp = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        return new Timestamp(timestamp);
    }

    private Double getTotalCost(Long sku, Integer qty) {
        String url = "http://localhost:8866/seller/getOrderPrice/" + sku + "/" + qty;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(Optional.empty(), httpHeaders);

        ResponseEntity<RestResponse> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, RestResponse.class);
        RestResponse restResponse = responseEntity.getBody();
        StatusCode statusCode = restResponse.getStatus();
        if (statusCode.getCode() == 200) {
            return (Double) restResponse.getData();
        } else {
            throw new ShopApplicationException(statusCode.getCode(), statusCode.getMessage());
        }
    }

    private void sendOrder(Long sku, Integer qty) {
        String url = "http://localhost:8866/seller/order/" + sku + "/" + qty;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(Optional.empty(), httpHeaders);

        ResponseEntity<RestResponse> response = restTemplate.exchange(url, HttpMethod.PUT, entity, RestResponse.class);
        log.info("订单已发送,sku {}, qty {}", sku, qty);

        RestResponse restResponse = response.getBody();
        StatusCode statusCode = restResponse.getStatus();
        if (statusCode.getCode() != 200) {
            throw new ShopApplicationException(statusCode.getCode(), statusCode.getMessage());
        }
    }

}


