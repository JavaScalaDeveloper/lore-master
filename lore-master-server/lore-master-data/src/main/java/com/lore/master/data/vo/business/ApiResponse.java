package com.lore.master.data.vo.business;

/**
 * 统一API响应实体
 */
public class ApiResponse<T> {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 错误码（可选）
     */
    private String errorCode;

    /**
     * 时间戳
     */
    private Long timestamp;

    // 构造函数
    public ApiResponse() {
    }

    public ApiResponse(Boolean success, String message, T data, String errorCode, Long timestamp) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errorCode = errorCode;
        this.timestamp = timestamp;
    }

    // Setter方法
    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    // Getter方法
    public Boolean getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage("操作成功");
        response.setData(data);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }

    /**
     * 成功响应（带自定义消息）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }

    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }

    /**
     * 失败响应（带错误码）
     */
    public static <T> ApiResponse<T> error(String errorCode, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setErrorCode(errorCode);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
}
