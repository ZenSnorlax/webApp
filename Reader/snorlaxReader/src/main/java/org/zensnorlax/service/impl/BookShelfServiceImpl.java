package org.zensnorlax.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zensnorlax.common.Result;
import org.zensnorlax.common.ZenException;
import org.zensnorlax.mapper.BookMapper;
import org.zensnorlax.mapper.UserCoBookMapper;
import org.zensnorlax.mapper.UserMapper;
import org.zensnorlax.model.pojo.User;
import org.zensnorlax.model.pojo.UserCollectBook;
import org.zensnorlax.model.vo.UserInfoVo;
import org.zensnorlax.service.BookShelfService;
import org.zensnorlax.model.pojo.Book;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zensnorlax
 * @version 1.0
 * @description: 用户服务实现类
 * @date 2025/3/22 18:39
 */
@SuppressWarnings("rawtypes")
@Service
public class BookShelfServiceImpl implements BookShelfService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    UserCoBookMapper userCoBookMapper;

    @Autowired
    BookMapper bookMapper;

    @Override
    public Result addBookToBookshelf(Long bookId, Long userId) {
        try {
            UserCollectBook userCollectBook = new UserCollectBook();
            userCollectBook.setUserId(userId);
            userCollectBook.setBookId(bookId);
            userCoBookMapper.insert(userCollectBook);
            return Result.success(null, "添加书籍到书架成功");
        } catch (Exception e) {
            throw new ZenException("添加书籍到书架失败");
        }
    }

    @Transactional(rollbackFor = ZenException.class)
    @Override
    public Result getBookshelf(Long id) {
        try {
            //根据用户id查询书架信息
            LambdaQueryWrapper<UserCollectBook> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(UserCollectBook::getUserId, id);
            List<UserCollectBook> list = userCoBookMapper.selectList(lambdaQueryWrapper);
            //构造userCollectBook book_id 映射关系
            List<Book> bookList = new ArrayList<>();
            if (list.size() > 0) {
                List<Long> bookIdList = list.stream().map(UserCollectBook::getBookId).collect(Collectors.toList());
                //根据bookIdList查询book信息
                bookMapper.selectByIds(bookIdList);
            }
            return Result.success(bookList, "获取书架信息成功");
        } catch (Exception e) {
            throw new ZenException("获取书架信息失败");
        }
    }

    @Override
    public Result removeBookFromBookshelf(Long id, Long bookId) {
        try {
            //根据用户id和bookId删除用户收藏的书籍信息
            LambdaQueryWrapper<UserCollectBook> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.allEq(Map.of(UserCollectBook::getUserId, id, UserCollectBook::getBookId, bookId));
            userCoBookMapper.delete(lambdaQueryWrapper);
            return Result.success(null, "从书架移除书籍成功");
        } catch (Exception e) {
            throw new ZenException("从书架移除书籍失败");
        }
    }
}
