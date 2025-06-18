package com.it.shop.bean;

public enum SellerResponseCode {

    PRICE_INVALID(11000, "价格非法值"),
    QTY_INVALID(11001, "商品数量非法值"),
    NO_SKU(11002, "商品SKU不存在"),
    NOT_ENOUGH_STORE(11003, "库存不足"),
    ACCOUNT_NOT_EXIST(11004, "账户不存在"),
    SETTLE_ERROR(11005, "资金结算异常"),
    SUCCESS(200, "Success");
    private Integer code;
    private String message;

    SellerResponseCode(Integer code, String message) {
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
