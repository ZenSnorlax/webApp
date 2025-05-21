package org.zensnorlax.util;

import java.util.Random;

/**
 * @author zensnorlax
 * @version 1.0
 * @description: 用于生成验证码的工具类
 * @date 2025/3/23 9:09
 */
public class VerificationCode {

    /**
     * 生成一个6位数字的验证码
     *
     * @return 返回生成的验证码字符串
     */
    public static String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            int digit = random.nextInt(10); // 生成0到9之间的随机数
            code.append(digit);
        }

        return code.toString();
    }

}
