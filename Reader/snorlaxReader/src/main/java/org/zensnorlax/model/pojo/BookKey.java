package org.zensnorlax.model.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author zensnorlax
 * @version 1.0
 * @description: TODO
 * @date 2025/3/21 14:12
 */
@TableName("book_key")
@Data
public class BookKey {

    @TableField("book_id")
    private Long bookId;

    @TableField("keyword_id")
    private Long keywordId;
}
