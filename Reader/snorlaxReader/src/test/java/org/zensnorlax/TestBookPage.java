package org.zensnorlax;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.zensnorlax.common.Result;
import org.zensnorlax.model.vo.BookSearchVo;
import org.zensnorlax.service.BookService;

import java.awt.geom.RectangularShape;
import java.util.List;

/**
 * @author zensnorlax
 * @version 1.0
 * @description: TODO
 * @date 2025/3/29 19:45
 */
@SpringBootTest
@SuppressWarnings("all")
public class TestBookPage {
    @Autowired
    private BookService bookService;

    @Test
    public void test() {
        BookSearchVo bookSearchVo = new BookSearchVo();
        bookSearchVo.setPageNum(1);
        bookSearchVo.setPageSize(10);
        bookSearchVo.setKeywords("毛泽东");
        Result result = bookService.search(bookSearchVo);
        System.out.println(result);
    }
}
