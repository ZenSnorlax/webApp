package org.zensnorlax.common;

import lombok.Data;

/**
 * @author zensnorlax
 * @version 1.0
 * @description 通用响应封装类
 * @date 2025/3/23 16:13
 */

@Data
public class Result<T> {
    // 状态码
    private Integer code;
    // 提示信息
    private String message;
    // 响应数据
    private T data;

    public Result(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 静态方法：返回成功响应
    public static <T> Result<T> success(T data) {
        return new Result<T>(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMessage(), data);
    }
    // 静态方法：返回成功响应
     public static <T> Result<T> success(T data, String message) {
        return new Result<T>(ResultEnum.SUCCESS.getCode(), message, data);
     }
    // 静态方法：返回失败响应
    public static <T> Result<T> error(ResultEnum resultEnum) {
        return new Result<T>(resultEnum.getCode(), resultEnum.getMessage());
    }
}
