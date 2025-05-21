package org.zensnorlax.model.vo;

import lombok.Data;

import java.util.List;

/**
 * @author zensnorlax
 * @version 1.0
 * @description: TODO
 * @date 2025/3/30 17:25
 */

@Data
public class BookSearchVo {
    private Integer pageNum;
    private Integer pageSize;
    private String keywords;
    private Integer total;
    private List<BookInfoVo> bookInfos;
}
