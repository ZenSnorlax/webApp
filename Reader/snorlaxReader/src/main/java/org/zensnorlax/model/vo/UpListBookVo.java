package org.zensnorlax.model.vo;

import lombok.Data;

import java.util.List;

/**
 * @author zensnorlax
 * @version 1.0
 * @description: TODO
 * @date 2025/3/30 20:12
 */
@Data
public class UpListBookVo {
    private Integer total;
    private Integer pageNum;
    private Integer pageSize;
    private List<BookInfoVo> bookInfos;
}
