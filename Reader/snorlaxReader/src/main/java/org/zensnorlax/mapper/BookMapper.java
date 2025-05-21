package org.zensnorlax.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.zensnorlax.model.pojo.Book;

import java.util.List;

@Mapper
public interface BookMapper extends BaseMapper<Book> {
    List<Book> selectByKeywords(Page<Book> page, @Param("keywords") List<String> keywords);
}

