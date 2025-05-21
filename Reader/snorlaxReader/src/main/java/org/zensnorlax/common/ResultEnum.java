package org.zensnorlax.common;

import lombok.Getter;

/**
 * 枚举类：定义响应的状态码及提示信息
 */
@Getter
public enum ResultEnum {
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    DATABASE_ERROR(503, "数据库操作失败"),
    VERIFYCODE_EXPIRED(401, "验证码已过期"),
    VERIFYCODE_ERROR(402, "验证码错误"),
    USER_NOT_EXIST(403, "用户不存在"),
    USER_PASSWORD_ERROR(404, "密码错误"),
    USER_ALREADY_EXIST(405, "用户已存在"), TOKEN_EXPIRED(406, "token已过期"),
    TOKEN_ERROR(407, "token错误"),
    NOT_LOGIN(408, "未登录"), FILE_TYPE_ERROR(409, "文件类型错误"),
    NOT_AUTHORITY(410, "无权限操作"),
    BOOK_NOT_FOUND(411, "图书不存在");

    private final Integer code;
    private final String message;

    ResultEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}