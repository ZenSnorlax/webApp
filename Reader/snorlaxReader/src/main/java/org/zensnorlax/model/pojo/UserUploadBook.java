package org.zensnorlax.model.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author zensnorlax
 * @version 1.0
 * @description: TODO
 * @date 2025/3/28 21:31
 */
@Data
@TableName("user_upload_book")
public class UserUploadBook {
    @TableField("user_id")
    private Long userId;
    @TableField("book_id")
    private Long bookId;
}
