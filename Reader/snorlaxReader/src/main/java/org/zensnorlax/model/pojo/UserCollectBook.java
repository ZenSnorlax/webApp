package org.zensnorlax.model.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author zensnorlax
 * @version 1.0
 * @description: TODO
 * @date 2025/4/3 15:07
 */
@TableName("user_collect_book")
@Data
public class UserCollectBook {
    private Long userId;
    private Long bookId;

    @TableField("collect_at")
    private Long collectTime;
}
