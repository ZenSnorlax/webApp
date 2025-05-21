package org.zensnorlax.model.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author zensnorlax
 * @version 1.0
 * @description: TODO
 * @date 2025/3/18 20:14
 */
@TableName("book")
@Data
public class Book {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String author;

    private String coverUrl;

    private String contentUrl;

    private Date publishDate;

    private String publisher;

    private String language;

    private String description;

    private Date createdAt;

    private Date updatedAt;
}
