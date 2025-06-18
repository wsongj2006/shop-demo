package com.it.shop.exception;

import com.it.shop.bean.StatusCode;

public class ShopApplicationException extends RuntimeException {
    private final StatusCode statusCode;

    public ShopApplicationException(int code, String message) {
        statusCode = new StatusCode();
        statusCode.setCode(code);
        statusCode.setMessage(message);
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }
}
