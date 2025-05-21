package org.zensnorlax.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author zensnorlax
 * @version 1.0
 * @description: TODO
 * @date 2025/3/18 21:19
 */

@Data
public class BookInfoVo {
    private Long id;
    private String coverUrl;
    private String title;
    private String author;
    private String publisher;
    private Date publishDate;
    private String description;
    private String language;
}
