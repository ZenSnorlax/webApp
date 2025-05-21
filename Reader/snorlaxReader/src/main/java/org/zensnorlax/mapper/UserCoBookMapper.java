package org.zensnorlax.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.zensnorlax.model.pojo.UserCollectBook;

@Mapper
public interface UserCoBookMapper extends BaseMapper<UserCollectBook> {
    @Insert("INSERT INTO user_collect_book (user_id, book_id) VALUES (#{userId}, #{bookId})")
    int insert(UserCollectBook userCollectBook);
}

