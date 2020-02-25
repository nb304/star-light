package org.king2.sl.data.cache;

import lombok.Data;
import org.king2.sl.data.dto.BookContentProviderDto;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 上传内容的cache
 */
@Data
public class UploadContentCache {

    private static final UploadContentCache UPLOAD_CONTENT_CACHE = new UploadContentCache();

    private UploadContentCache() {
    }

    public static UploadContentCache getInstance() {
        return UPLOAD_CONTENT_CACHE;
    }

    /**
     * 缓存信息
     * bookId --> 内容信息集合
     */
    private final Map<String, List<BookContentProviderDto>> cache = new ConcurrentHashMap<>();

    /**
     * 锁
     */
    public static final ReentrantReadWriteLock LOCK = new ReentrantReadWriteLock();

    /**
     * 写锁的监听对象
     */
    public static final Condition W_COND = LOCK.writeLock().newCondition();
}
