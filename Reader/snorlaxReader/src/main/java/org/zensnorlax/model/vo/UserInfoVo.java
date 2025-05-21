package org.zensnorlax.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;

/**
 * @author zensnorlax
 * @version 1.0
 * @description: TODO
 * @date 2025/3/25 18:30
 */

@Data
public class UserInfoVo {
    private String email;
    private String nickname;
    private Date createdAt;
}
