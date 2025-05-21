package org.zensnorlax.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.zensnorlax.model.pojo.BookKey;


@Mapper
public interface BookKeyMapper extends BaseMapper<BookKey> {
    @Insert("INSERT INTO book_key (book_id, keyword_id) VALUES (#{bookId}, #{keywordId})")
    int insert(BookKey bookKey);
}
