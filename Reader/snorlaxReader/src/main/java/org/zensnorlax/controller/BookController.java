package org.zensnorlax.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zensnorlax.common.CheckCookie;
import org.zensnorlax.common.Result;
import org.zensnorlax.model.vo.BookSearchVo;
import org.zensnorlax.model.vo.UpListBookVo;
import org.zensnorlax.service.BookService;

/**
 * @author zensnorlax
 * @version 1.0
 * @description: TODO
 * @date 2025/3/18 16:44
 */

@Slf4j
@RestController
@RequestMapping("/book")
@CrossOrigin(origins = "http://localhost:1421", allowCredentials = "true")
@SuppressWarnings("rawtypes")
public class BookController {
    @Autowired
    private BookService bookService;

    //搜索图书
    @GetMapping("/search")
    public Result search(BookSearchVo bookSearchVo) {
        return bookService.search(bookSearchVo);
    }

    //上传图书
    @PostMapping("/upload")
    @CheckCookie
    public Result upload(@RequestParam("file") MultipartFile file) {
        return bookService.upload(file);
    }

    //查看上传的图书列表
    @GetMapping("/upload/list")
    @CheckCookie
    public Result uploadList(UpListBookVo upListBookVo) {
        return bookService.uploadList(upListBookVo);
    }

    //删除图书
    @CheckCookie
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Long bookId) {
        return bookService.delete(bookId);
    }

    // 获取临时图书实体链接
    @CheckCookie
    @GetMapping("/{id}")
    public Result generateDownloadUrl(@PathVariable("id") Long bookId) {
        return bookService.generateDownloadUrl(bookId);
    }

    // 获取图书目录
    @CheckCookie
    @GetMapping("/{id}/catalog")
    public Result getCatalog(@PathVariable("id") Long bookId) {
        return bookService.getCatalog(bookId);
    }

    //
}
