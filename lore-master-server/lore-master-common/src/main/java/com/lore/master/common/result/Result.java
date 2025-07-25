package com.lore.master.common.result;

import lombok.Data;

/**
 * 统一返回结果类
 */
@Data
public class Result<T> {
    
    /**
     * 成功状态码
     */
    public static final int SUCCESS_CODE = 200;
    
    /**
     * 失败状态码
     */
    public static final int ERROR_CODE = 500;
    
    /**
     * 状态码
     */
    private int code;
    
    /**
     * 返回消息
     */
    private String message;
    
    /**
     * 返回数据
     */
    private T data;
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 时间戳
     */
    private long timestamp;
    
    public Result() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public Result(int code, String message, T data, boolean success) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = success;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 成功返回（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(SUCCESS_CODE, "操作成功", null, true);
    }
    
    /**
     * 成功返回（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, "操作成功", data, true);
    }
    
    /**
     * 成功返回（带消息和数据）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(SUCCESS_CODE, message, data, true);
    }
    
    /**
     * 失败返回（带消息）
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(ERROR_CODE, message, null, false);
    }
    
    /**
     * 失败返回（带状态码和消息）
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null, false);
    }
    
    /**
     * 失败返回（带状态码、消息和数据）
     */
    public static <T> Result<T> error(int code, String message, T data) {
        return new Result<>(code, message, data, false);
    }
}
