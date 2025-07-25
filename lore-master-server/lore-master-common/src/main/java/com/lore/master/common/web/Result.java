package com.lore.master.common.web;

public class Result<T> {
    private boolean success;
    private String message;
    private T data;
    private Long total;

    public Result() {}

    public Result(boolean success, String message, T data, Long total) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.total = total;
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(true, null, data, null);
    }
    public static <T> Result<T> ok(T data, Long total) {
        return new Result<>(true, null, data, total);
    }
    public static <T> Result<T> fail(String message) {
        return new Result<>(false, message, null, null);
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public Long getTotal() { return total; }
    public void setTotal(Long total) { this.total = total; }
} 