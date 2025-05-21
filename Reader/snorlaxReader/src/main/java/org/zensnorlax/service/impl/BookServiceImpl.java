package org.zensnorlax.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.RemoveObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.messages.Item;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import nl.siegmann.epublib.domain.Date;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.zensnorlax.common.Result;
import org.zensnorlax.common.ResultEnum;
import org.zensnorlax.common.ZenException;
import org.zensnorlax.config.JsonRedisTemplate;
import org.zensnorlax.mapper.BookKeyMapper;
import org.zensnorlax.mapper.BookMapper;
import org.zensnorlax.mapper.KeywordMapper;
import org.zensnorlax.mapper.UserUpBookMapper;
import org.zensnorlax.model.pojo.Book;
import org.zensnorlax.model.pojo.BookKey;
import org.zensnorlax.model.pojo.Keyword;
import org.zensnorlax.model.pojo.UserUploadBook;
import org.zensnorlax.model.vo.BookInfoVo;
import org.zensnorlax.model.vo.BookSearchVo;
import org.zensnorlax.model.vo.UpListBookVo;
import org.zensnorlax.service.BookService;
import org.zensnorlax.util.AuthContextHolder;
import org.zensnorlax.util.LanguageTokenizer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Slf4j
@SuppressWarnings("all")
public class BookServiceImpl implements BookService {

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bookContentBucket}")
    private String bookContentBucket;

    @Value("${minio.bookCoverBucket}")
    private String bookCoverBucket;

    @Value("${minio.endpoint}")
    private String minioEndpoint;

    @Autowired
    private KeywordMapper keywordMapper;

    @Autowired
    private BookKeyMapper bookKeyMapper;

    @Autowired
    private UserUpBookMapper userUpBookMapper;

    @Autowired
    private JsonRedisTemplate jsonRedisTemplate;

    /**
     * 根据关键词搜索书籍
     */
    @Override
    public Result search(BookSearchVo bookSearchVo) {
        // 设置分页参数
        Page<Book> page = new Page<>(bookSearchVo.getPageNum(), bookSearchVo.getPageSize());
        // 查找 Redis 缓存
        List<String> tokens = (List<String>) jsonRedisTemplate.opsForValue().get(bookSearchVo.getKeywords());
        if (tokens == null || tokens.isEmpty()) {
            // 缓存中没有，则从数据库查找
            tokens = LanguageTokenizer.tokenizeMixedText(bookSearchVo.getKeywords());
            jsonRedisTemplate.opsForValue().set(bookSearchVo.getKeywords(), tokens, 10, TimeUnit.MINUTES);
        }
        // 根据关键词搜索书籍
        List<Book> books = bookMapper.selectByKeywords(page, tokens);
        bookSearchVo.setTotal((int) page.getTotal());
        bookSearchVo.setBookInfos(books.stream().map(book -> {
            BookInfoVo vo = new BookInfoVo();
            BeanUtils.copyProperties(book, vo);
            return vo;
        }).collect(Collectors.toList()));
        return Result.success(bookSearchVo);
    }

    /**
     * 上传书籍文件并处理相关元数据
     */
    @Override
    @Transactional
    public Result upload(MultipartFile file) {
        if (!isValidEpubFile(file)) {
            return Result.error(ResultEnum.FILE_TYPE_ERROR);
        }

        Book book = new Book();
        try {
            byte[] contentBytes = file.getBytes();
            String fileName = file.getOriginalFilename();
            book.setTitle(fileName != null ? fileName : "Untitled");
            bookMapper.insert(book); // 插入获取ID

            // 异步处理文件上传和元数据提取
            // 修改为解压后的内容上传
            CompletableFuture<String> contentUrlFuture = uploadDecompressedContent(book.getId(), contentBytes);
            CompletableFuture<Book> metadataFuture = processEpubMetadata(contentBytes, book.getId());

            // 等待结果并合并
            String contentUrl = contentUrlFuture.get();
            Book metadataBook = metadataFuture.get();

            // 更新 Book 对象
            updateBookFromMetadata(book, metadataBook);
            book.setContentUrl(contentUrl);

            bookMapper.updateById(book); // 统一更新

            UserUploadBook userUploadBook = new UserUploadBook();
            userUploadBook.setBookId(book.getId());
            Long userId = AuthContextHolder.getUserId();
            userUploadBook.setUserId(userId);
            userUpBookMapper.insert(userUploadBook); // 记录用户上传记录

            BookInfoVo vo = new BookInfoVo();
            BeanUtils.copyProperties(book, vo);

            insertKeywords(book); // 异步插入关键词
            return Result.success(vo, "上传成功");
        } catch (Exception e) {
            log.error("上传失败", e);
            if (book.getId() != null) {
                try {
                    bookMapper.deleteById(book.getId());
                    // 删除 MinIO 中的书籍内容和封面
                    deleteObjectsByPrefix(bookContentBucket, book.getId() + "/").get();
                    deleteObjectsByPrefix(bookCoverBucket, book.getId() + "/").get();
                } catch (Exception ex) {
                    log.error("回滚过程中发生错误", ex);
                }
            }
            throw new ZenException("上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证文件是否为有效的 EPUB 格式
     */
    protected boolean isValidEpubFile(MultipartFile file) {
        if (file.isEmpty() || file.getOriginalFilename() == null) {
            return false;
        }
        String fileName = file.getOriginalFilename().toLowerCase();
        if (!fileName.endsWith(".epub")) {
            return false;
        }
        // 检查文件头是否为 ZIP 格式
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[4];
            if (is.read(header) != 4) {
                return false;
            }
            // ZIP 文件头：0x50 0x4B 0x03 0x04
            return (header[0] == 0x50 && header[1] == 0x4B && header[2] == 0x03 && header[3] == 0x04);
        } catch (IOException e) {
            log.error("文件头读取失败", e);
            return false;
        }
    }

    /**
     * 异步上传解压后的书籍内容到 MinIO 并返回入口文件的 URL（例如 index.html）
     */
    @Async("asyncExecutor")
    protected CompletableFuture<String> uploadDecompressedContent(Long bookId, byte[] contentBytes) {
        try (InputStream inputStream = new ByteArrayInputStream(contentBytes);
             ZipInputStream zis = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            boolean hasIndex = false;
            // 遍历解压包中的每个条目
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    // 构造对象名，保持目录结构，如：{bookId}/xxx/xxx.html
                    String objectName = bookId + "/" + entry.getName();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }
                    byte[] fileData = baos.toByteArray();
                    // 上传当前解压出的文件
                    minioClient.putObject(
                            PutObjectArgs.builder()
                                    .bucket(bookContentBucket)
                                    .object(objectName)
                                    .stream(new ByteArrayInputStream(fileData), fileData.length, -1)
                                    .build()
                    );

                }
                zis.closeEntry();
            }
            String entryFile = bookId + "/";
            return CompletableFuture.completedFuture(entryFile);
        } catch (Exception e) {
            log.error("解压并上传文件失败", e);
            return CompletableFuture.failedFuture(new ZenException("解压并上传文件失败: " + e.getMessage()));
        }
    }

    /**
     * 异步处理 EPUB 文件的元数据并返回书籍对象
     */
    @Async("asyncExecutor")
    protected CompletableFuture<Book> processEpubMetadata(byte[] contentBytes, Long bookId) {
        Book metadataBook = new Book();
        try (InputStream inputStream = new ByteArrayInputStream(contentBytes)) {
            EpubReader epubReader = new EpubReader();
            nl.siegmann.epublib.domain.Book epubBook = epubReader.readEpub(inputStream);
            Metadata metadata = epubBook.getMetadata();

            metadataBook.setTitle(getFirstValue(metadata.getTitles()));
            metadataBook.setAuthor(getAuthors(metadata));
            metadataBook.setPublisher(getFirstValue(metadata.getPublishers()));
            metadataBook.setDescription(getFirstValue(metadata.getDescriptions()));
            metadataBook.setPublishDate(parseEpubDate(getFirstDate(metadata.getDates())));

            String coverUrl = uploadCoverImage(bookId, epubBook);
            metadataBook.setCoverUrl(coverUrl);

            return CompletableFuture.completedFuture(metadataBook);
        } catch (Exception e) {
            log.error("元数据处理失败", e);
            return CompletableFuture.failedFuture(new ZenException("元数据处理失败: " + e.getMessage()));
        }
    }

    /**
     * 上传书籍封面图片到 MinIO 并返回 URL
     */
    private String uploadCoverImage(Long bookId, nl.siegmann.epublib.domain.Book epubBook) {
        Resource coverImage = epubBook.getCoverImage();
        if (coverImage == null) {
            log.warn("未找到封面图片");
            return null;
        }

        try (InputStream coverStream = coverImage.getInputStream()) {
            String objectName = bookId + "/" + extractFileName(coverImage.getHref());
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bookCoverBucket)
                            .object(objectName)
                            .stream(coverStream, coverImage.getSize(), -1)
                            .build()
            );
            return minioEndpoint + "/" + bookCoverBucket + "/" + objectName;
        } catch (Exception e) {
            log.error("封面上传失败", e);
            throw new ZenException("封面上传失败: " + e.getMessage());
        }
    }

    /**
     * 从资源的 URL 中提取文件名
     */
    private String extractFileName(String href) {
        if (href == null) return "cover";
        int lastSlash = href.lastIndexOf('/');
        return lastSlash == -1 ? href : href.substring(lastSlash + 1);
    }

    /**
     * 从元数据中更新书籍信息
     */
    private void updateBookFromMetadata(Book target, Book source) {
        if (source.getTitle() != null) target.setTitle(source.getTitle());
        if (source.getAuthor() != null) target.setAuthor(source.getAuthor());
        if (source.getPublisher() != null) target.setPublisher(source.getPublisher());
        if (source.getDescription() != null) target.setDescription(source.getDescription());
        if (source.getPublishDate() != null) target.setPublishDate(source.getPublishDate());
        if (source.getCoverUrl() != null) target.setCoverUrl(source.getCoverUrl());
    }

    /**
     * 从书籍元数据中获取作者列表
     */
    private String getAuthors(Metadata metadata) {
        return metadata.getAuthors().stream()
                .map(author -> {
                    String first = author.getFirstname() != null ? author.getFirstname() : "";
                    String last = author.getLastname() != null ? author.getLastname() : "";
                    return (first + " " + last).trim();
                })
                .filter(name -> !name.isEmpty())
                .collect(Collectors.joining(", "));
    }

    /**
     * 获取列表中的第一个值，如果列表为空则返回 null
     */
    private String getFirstValue(List<String> list) {
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 获取日期列表中的第一个日期对象，如果列表为空则返回 null
     */
    private Date getFirstDate(List<Date> dates) {
        return dates.isEmpty() ? null : dates.get(0);
    }

    /**
     * 解析 EPUB 格式的日期对象为 Java 的 Date 类型
     */
    private java.util.Date parseEpubDate(Date epubDate) {
        if (epubDate == null) return null;
        String dateStr = epubDate.getValue();
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
        } catch (ParseException e) {
            log.warn("日期解析失败: {}", dateStr, e);
            return null;
        }
    }

    /**
     * 异步插入书籍相关的关键词
     */
    @Async("asyncExecutor")
    @Transactional
    protected CompletableFuture<Void> insertKeywords(Book book) {
        String text = book.getTitle() + book.getAuthor();
        List<String> tokens = LanguageTokenizer.tokenizeMixedText(text);
        if (tokens == null || tokens.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        // 批量查询已有的关键词
        QueryWrapper<Keyword> keywordQuery = new QueryWrapper<>();
        keywordQuery.in("keyword", tokens);
        List<Keyword> existingKeywords = keywordMapper.selectList(keywordQuery);

        Map<String, Keyword> keywordMap = existingKeywords.stream()
                .collect(Collectors.toMap(Keyword::getKeyword, k -> k));

        List<Keyword> keywordsToInsert = new ArrayList<>();
        for (String token : tokens) {
            if (!keywordMap.containsKey(token)) {
                Keyword k = new Keyword();
                k.setKeyword(token);
                keywordsToInsert.add(k);
            }
        }

        if (!keywordsToInsert.isEmpty()) {
            keywordMapper.insert(keywordsToInsert);
            keywordQuery = new QueryWrapper<>();
            keywordQuery.in("keyword", tokens);
            List<Keyword> allKeywords = keywordMapper.selectList(keywordQuery);
            keywordMap = allKeywords.stream()
                    .collect(Collectors.toMap(Keyword::getKeyword, k -> k));
        }

        // 处理关联关系
        List<Long> keywordIds = keywordMap.values().stream()
                .map(Keyword::getId)
                .collect(Collectors.toList());

        List<BookKey> existingBookKeys;
        if (keywordIds.isEmpty()) {
            existingBookKeys = Collections.emptyList();
        } else {
            QueryWrapper<BookKey> bkQuery = new QueryWrapper<>();
            bkQuery.eq("book_id", book.getId()).in("keyword_id", keywordIds);
            existingBookKeys = bookKeyMapper.selectList(bkQuery);
        }

        Set<Long> existingKeywordIdSet = existingBookKeys.stream()
                .map(BookKey::getKeywordId)
                .collect(Collectors.toSet());

        List<BookKey> bookKeysToInsert = new ArrayList<>();
        for (String token : tokens) {
            Keyword k = keywordMap.get(token);
            if (k != null && !existingKeywordIdSet.contains(k.getId())) {
                BookKey bookKey = new BookKey();
                bookKey.setBookId(book.getId());
                bookKey.setKeywordId(k.getId());
                bookKeysToInsert.add(bookKey);
            }
        }

        if (!bookKeysToInsert.isEmpty()) {
            bookKeyMapper.insert(bookKeysToInsert);
        }

        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> deleteObjectsByPrefix(String bucketName, String prefix) {
        try {
            // 列出所有匹配前缀的对象
            Iterable<io.minio.Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(prefix)
                            .recursive(true)
                            .build()
            );

            // 遍历并删除每个对象
            for (io.minio.Result<Item> result : results) {
                Item item = result.get();
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(item.objectName())
                                .build()
                );
                log.info("已删除对象: {}/{}", bucketName, item.objectName());
            }
        } catch (Exception e) {
            log.error("删除存储桶 {} 中前缀为 {} 的对象失败: {}", bucketName, prefix, e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * 获取用户上传的书籍列表
     */
    @Override
    public Result uploadList(UpListBookVo upListBookVo) {
        Long userId = AuthContextHolder.getUserId();

        // 分页查询用户上传记录
        Page<UserUploadBook> page = new Page<>(upListBookVo.getPageNum(), upListBookVo.getPageSize());
        Page<UserUploadBook> userUploadBookPage = userUpBookMapper.selectPage(page, new QueryWrapper<UserUploadBook>()
                .eq("user_id", userId));
        List<UserUploadBook> userUploadBooks = userUploadBookPage.getRecords();

        // 批量查询书籍，避免 N+1 查询问题
        List<Long> bookIds = userUploadBooks.stream()
                .map(UserUploadBook::getBookId)
                .collect(Collectors.toList());
        List<Book> books = bookMapper.selectByIds(bookIds);

        // 构建书籍映射（bookId -> Book）保证上传记录的顺序一致
        Map<Long, Book> bookMap = books.stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));

        // 映射转换成 BookInfoVo
        List<BookInfoVo> bookInfoVos = userUploadBooks.stream()
                .map(userUploadBook -> {
                    Book book = bookMap.get(userUploadBook.getBookId());
                    BookInfoVo vo = new BookInfoVo();
                    if (book != null) {
                        BeanUtils.copyProperties(book, vo);
                    }
                    return vo;
                })
                .collect(Collectors.toList());
        upListBookVo.setTotal((int) page.getTotal());
        upListBookVo.setBookInfos(bookInfoVos);
        return Result.success(upListBookVo);
    }

    /**
     * 删除用户上传的书籍
     */
    @Override
    @Transactional
    public Result delete(Long bookId) {
        Long userId = AuthContextHolder.getUserId();
        UserUploadBook userUploadBook = userUpBookMapper.selectOne(new QueryWrapper<UserUploadBook>()
                .eq("user_id", userId)
                .eq("book_id", bookId));
        if (userUploadBook == null) {
            return Result.error(ResultEnum.BOOK_NOT_FOUND);
        }
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            return Result.error(ResultEnum.BOOK_NOT_FOUND);
        }
        bookMapper.deleteById(bookId);
        try {
            deleteObjectsByPrefix(bookContentBucket, book.getId() + "/").get();
            deleteObjectsByPrefix(bookCoverBucket, book.getId() + "/").get();
        } catch (Exception e) {
            log.error("删除Minio中的书籍内容和封面失败", e);
            throw new ZenException("删除失败: " + e.getMessage());
        }
        return Result.success("删除成功");
    }

    @Override
    public Result generateDownloadUrl(Long bookId) {
        // 使用 presignedGetObject 方法生成预签名的 URL，设置过期时间为 2 小时
        Book book = bookMapper.selectById(bookId);
        try {
            return Result.success(minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bookContentBucket)
                            .object(book.getContentUrl())
                            .expiry(60)
                            .build()));
        } catch (Exception e) {
            log.error("生成临时URL失败", e);
            throw new ZenException("生成临时URL失败: " + e.getMessage());
        }
    }

    // 获取书籍目录
    @Override
    public Result getCatalog(Long bookId) {
        return null;
    }
}
