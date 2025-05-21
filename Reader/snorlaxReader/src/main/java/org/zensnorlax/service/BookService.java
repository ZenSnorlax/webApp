package org.zensnorlax.service;

import org.springframework.web.multipart.MultipartFile;
import org.zensnorlax.common.Result;
import org.zensnorlax.model.vo.BookSearchVo;
import org.zensnorlax.model.vo.UpListBookVo;

@SuppressWarnings("rawtypes")
public interface BookService {

    /**
     * 根据关键词搜索书籍
     *
     * @param bookSearchVo 包含搜索条件的VO
     * @return 包含BookInfoVo列表的结果
     */
    Result search(BookSearchVo bookSearchVo);

    /**
     * 上传图书
     *
     * @param file 文件
     * @return 结果
     */
    Result upload(MultipartFile file);

    /**
     * 上传图书列表
     *
     * @return 结果
     */
    Result uploadList(UpListBookVo upListBookVo);

    /**
     * 删除图书
     *
     * @param book_id 图书ID
     * @return 结果
     */
    Result delete(Long book_id);

    /**
     * 获取图书内容临时URL
     *
     * @param bookId 图书ID
     * @return 结果
     */
    Result generateDownloadUrl(Long bookId);

    /**
     * 获取图书目录
     *
     * @param bookId 图书ID
     * @return 结果
     */
    Result getCatalog(Long bookId);


}
