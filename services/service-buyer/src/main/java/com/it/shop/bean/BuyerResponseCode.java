package com.it.shop.bean;

public enum BuyerResponseCode {


    USER_NAME_REQUIRED(10010, "用户名不能为空"),
    USER_NOT_EXIST(10011, "用户不存在，非法操作"),
    MONEY_IN_VALIDE(10012, "充值金额合法区间为1到10000"),
    BALANCE_IN_VALID(10013, "余额不足"),
    PRICE_QUERY_FAILED(10014, "订单价格获取失败"),
    QTY_IN_VALID(10015, "商品数量非法值"),
    SUCCESS(200, "Success");

    private Integer code;
    private String message;

    BuyerResponseCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
