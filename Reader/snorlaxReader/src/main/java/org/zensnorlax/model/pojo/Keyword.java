package org.zensnorlax.model.pojo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author zensnorlax
 * @version 1.0
 * @description: TODO
 * @date 2025/3/21 14:12
 */
@TableName("keyword")
@Data
public class Keyword {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String keyword;
    private String createdAt;
}
