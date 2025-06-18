package com.it.shop.bean;

public class RestResponse <T>{
    private T data;
    private StatusCode status;

    public static RestResponse create() {
        return new RestResponse();
    }

    public RestResponse buildStatus(Integer code, String msg) {
        StatusCode statusCode = new StatusCode();
        statusCode.setMessage(msg);
        statusCode.setCode(code);
        this.setStatus(statusCode);
        return this;
    }

    public RestResponse buildStatus(StatusCode status) {
        this.setStatus(status);
        return this;
    }


    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public StatusCode getStatus() {
        return status;
    }

    public void setStatus(StatusCode status) {
        this.status = status;
    }
}
