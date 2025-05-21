package org.zensnorlax.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zensnorlax.common.CheckCookie;
import org.zensnorlax.common.Result;
import org.zensnorlax.service.BookShelfService;

/**
 * @author zensnorlax
 * @version 1.0
 * @description: 用户控制器
 * @date 2025/3/22 18:38
 */

@Slf4j
@RestController
@RequestMapping("/bookshelf")
@CrossOrigin(origins = "http://localhost:1421", allowCredentials = "true")
@SuppressWarnings("rawtypes")
public class BookShelfController {
    @Autowired
    private BookShelfService bookShelfService;

    //添加图书至用户书架
    @PostMapping("/{id}/bookshelf")
    @CheckCookie
    public Result addBookToBookshelf(@RequestParam("book_id") Long bookId, @PathVariable("id") Long userId) {
        return bookShelfService.addBookToBookshelf(bookId, userId);
    }

    //查看用户书架
    @GetMapping("/{id}/bookshelf")
    @CheckCookie
    public Result getBookshelf(@PathVariable Long id) {
        return bookShelfService.getBookshelf(id);
    }

    //从用户书架移除图书
    @DeleteMapping("/{id}/bookshelf/{book_id}")
    @CheckCookie
    public Result removeBookFromBookshelf(@PathVariable("id") Long id, @PathVariable("book_id") Long bookId) {
        return bookShelfService.removeBookFromBookshelf(id, bookId);
    }
}
