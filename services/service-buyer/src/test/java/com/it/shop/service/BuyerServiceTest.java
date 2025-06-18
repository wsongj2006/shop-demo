package com.it.shop.service;

import com.it.shop.bean.*;
import com.it.shop.exception.ShopApplicationException;
import com.it.shop.repository.UserAccountRepository;
import com.it.shop.repository.UserOrderRepository;
import com.it.shop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BuyerServiceTest {

    private BuyerService buyerService;
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private UserOrderRepository userOrderRepository;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    public void before() {
        buyerService = new BuyerService(userRepository, userAccountRepository, userOrderRepository, restTemplate);
    }

    @Test
    //测试用户创建,提供正确值
    public void testCreateUserSuccess() {
        User user = createTestUser(11l, "TEST", "TESTADD").get();
        when(userRepository.save(user)).thenReturn(user);
        User createduser = buyerService.createUser(user);
        Assert.notNull(createduser, "");
    }

    @Test
    //测试用户创建, 提供非法值
    public void testCreateUserFailed() {
        User user = createTestUser(11l, "", "TESTADD").get();
        User createduser = null;
        try {
            createduser = buyerService.createUser(user);
        } catch (ShopApplicationException e) {
            System.out.println(e.getStatusCode().getMessage());
        }
        Assert.isNull(createduser, "");
    }

    @Test
    //测试给新用户充值
    public void testAddMoneyToNewUserAccount() {
        Optional<User> userOpt = createTestUser(11l, "TEST", "TESTADD");

        when(userRepository.findById(11l)).thenReturn(userOpt);

        UserAccount newUserAccount = new UserAccount();
        newUserAccount.setBalance(100d);
        newUserAccount.setUserId(11l);
        newUserAccount.setUpdatedAt(null);

        when(userAccountRepository.findByUserId(11l)).thenReturn(Optional.empty());
        when(userAccountRepository.save(any())).thenReturn(newUserAccount);

        UserAccount savedUserAccount = buyerService.addMoney(11l, 100);

        Assert.isTrue(savedUserAccount.getBalance() == 100d, "");
    }

    @Test
    //测试给已有用户充值
    public void testAddMoneyToExistingUserAccount() {
        Optional<User> userOpt = createTestUser(11l, "TEST", "TESTADD");

        when(userRepository.findById(11l)).thenReturn(userOpt);

        UserAccount existingUserAccount = new UserAccount();
        existingUserAccount.setBalance(100d);
        existingUserAccount.setUserId(11l);
        Optional<UserAccount> userAccountOpt = Optional.of(existingUserAccount);

        when(userAccountRepository.findByUserId(11l)).thenReturn(userAccountOpt);
        when(userAccountRepository.save(any())).thenReturn(existingUserAccount);

        UserAccount savedUserAccount = buyerService.addMoney(11l, 200);

        Assert.isTrue(savedUserAccount.getBalance() == 300d, "");
    }

    @Test
    //测试下单，扣款后的余额
    public void testOrder() {
        Long sku = 11l;
        Long userId = 11l;
        int qty = 10;
        Optional<User> userOpt = createTestUser(11l, "TEST", "TESTADD");
        when(userRepository.findById(11l)).thenReturn(userOpt);

        RestResponse<Double> orderPriceRestResponse = new RestResponse();
        orderPriceRestResponse.setData(20d);
        HttpStatusCode httpStatusCode = HttpStatusCode.valueOf(200);
        StatusCode statusCode = new StatusCode();
        statusCode.setCode(200);
        orderPriceRestResponse.setStatus(statusCode);
        ResponseEntity<RestResponse> orderPriceResponseEntity = new ResponseEntity<RestResponse>(orderPriceRestResponse, httpStatusCode);

        when(restTemplate.exchange(any(String.class), eq(HttpMethod.GET), any(), eq(RestResponse.class)))
                .thenReturn(orderPriceResponseEntity);


        UserAccount existingUserAccount = new UserAccount();
        existingUserAccount.setBalance(100d);
        existingUserAccount.setUserId(11l);
        Optional<UserAccount> userAccountOpt = Optional.of(existingUserAccount);

        when(userAccountRepository.findByUserId(11l)).thenReturn(userAccountOpt);
        when(userAccountRepository.save(any())).thenReturn(existingUserAccount);

        UserOrder userOrder = new UserOrder();
        userOrder.setUserId(userId);
        userOrder.setQty(qty);
        userOrder.setSku(sku);
        when(userOrderRepository.save(any())).thenReturn(userOrder);


        RestResponse sendOrderRestResponse = new RestResponse();
        ResponseEntity<RestResponse> sendOrderResponseEntity = new ResponseEntity<RestResponse>(sendOrderRestResponse, httpStatusCode);
        sendOrderRestResponse.setStatus(statusCode);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.PUT), any(), eq(RestResponse.class)))
                .thenReturn(sendOrderResponseEntity);

        buyerService.order(userId, sku, qty);

        Assert.isTrue(existingUserAccount.getBalance() == 80d, "");

    }

    private Optional<User> createTestUser(Long id, String name, String address) {
        User user = new User();
        user.setId(id);
        user.setUserName(name);
        user.setAddress(address);
        return Optional.of(user);
    }
}
