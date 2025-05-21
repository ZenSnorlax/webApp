package org.zensnorlax.service;

import org.zensnorlax.common.Result;

/**
 * 用户服务接口，定义了获取用户信息的方法。
 */
@SuppressWarnings("rawtypes")
public interface BookShelfService {


    /**
     * 添加书籍到书架
     *
     * @param bookId 书籍ID
     * @return 添加书籍到书架的结果对象
     */
    Result addBookToBookshelf(Long bookId, Long userId);

    /**
     * 获取书架的信息
     *
     * @param userId 用户ID
     * @return 获取书架的结果对象
     */
    Result getBookshelf(Long userId);

    /**
     * 移除书籍
     *
     * @param userId 用户ID
     * @param bookId 书籍ID
     * @return 移除书籍的结果对象
     */
    Result removeBookFromBookshelf(Long userId, Long bookId);
}

