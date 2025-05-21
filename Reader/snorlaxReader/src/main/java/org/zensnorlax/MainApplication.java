package org.zensnorlax;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author zensnorlax
 * @version 1.0
 * @description: TODO
 * @date 2025/3/18 18:22
 */
@EnableTransactionManagement
@MapperScan("org.zensnorlax.mapper")
@SpringBootApplication
@EnableAsync
public class MainApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}
