package org.zensnorlax;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.zensnorlax.service.BookShelfService;

/**
 * @author zensnorlax
 * @version 1.0
 * @description: TODO
 * @date 2025/4/3 16:31
 */

@SpringBootTest
public class TestBookBookShelf {

    @Autowired
    BookShelfService bookShelfService;

    @Test
    public void addBookToBookshelf() {
        bookShelfService.addBookToBookshelf(130L, 2L);
    }

    @Test
    public void removeBookFromBookshelf() {
        bookShelfService.removeBookFromBookshelf(2L, 130L);
    }

    @Test
    public void getBookShelf() {
        bookShelfService.getBookshelf(130L);
    }
}
