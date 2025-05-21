package org.zensnorlax;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.zensnorlax.common.Result;
import org.zensnorlax.service.UserService;

/**
 * @author zensnorlax
 * @version 1.0
 * @description: TODO
 * @date 2025/4/3 16:32
 */

@SpringBootTest
@SuppressWarnings("all")
public class TestBookUser {
    @Autowired
    private UserService userService;

    @Test
    public void getUserInfo() {
      userService.getUserInfo(2L);
    }

}
