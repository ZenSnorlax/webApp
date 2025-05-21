package org.zensnorlax.model.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author zensnorlax
 * @version 1.0
 * @description: TODO
 * @date 2025/4/4 10:31
 */

@TableName("follow")
@Data
public class Follow {
    @TableField("follower_id")
    private Long followerId;

    @TableField("followee_id")
    private Long followeeId;
}
