package org.zensnorlax;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.zensnorlax.common.ZenException;
import org.zensnorlax.service.BookService;

/**
 * @author zensnorlax
 * @version 1.0
 * @description: TODO
 * @date 2025/3/31 16:29
 */
@SpringBootTest
public class TestBookGenerateTemporaryUrl {
    @Autowired
    private BookService bookService;

    @Test
    public void testGenerateTemporaryUrl() throws ZenException {
        System.out.println(bookService.generateDownloadUrl(130L));
    }
}
