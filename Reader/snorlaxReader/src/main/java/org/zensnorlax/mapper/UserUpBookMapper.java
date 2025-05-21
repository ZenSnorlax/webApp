package org.zensnorlax.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.zensnorlax.model.pojo.UserUploadBook;

@Mapper
public interface UserUpBookMapper extends BaseMapper<UserUploadBook> {
    @Insert("INSERT INTO user_upload_book (user_id, book_id) VALUES (#{userId}, #{bookId})")
    int insert(UserUploadBook userUploadBook);
}
