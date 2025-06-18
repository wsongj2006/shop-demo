package com.it.shop.bean;

public class StatusCode {
    private int code;
    private String message;

    public static StatusCode create(int code, String message) {
        StatusCode statusCode = new StatusCode();
        statusCode.setCode(code);
        statusCode.setMessage(message);
        return statusCode;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
