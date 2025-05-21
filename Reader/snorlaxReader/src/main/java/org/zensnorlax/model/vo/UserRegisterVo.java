package org.zensnorlax.model.vo;

import lombok.Data;

/**
 * @author zensnorlax
 * @version 1.0
 * @description: TODO
 * @date 2025/3/23 11:01
 */

@Data
public class UserRegisterVo {
    private String email;

    private String password;

    private String nickname;

    private String verifyCode;
}
