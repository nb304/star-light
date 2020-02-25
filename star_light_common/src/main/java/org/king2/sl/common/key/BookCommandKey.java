package org.king2.sl.common.key;

/**
 * 书本的操作key集合
 */
public class BookCommandKey {

    /**
     * 书本数据类型（小说、漫画）,存在REDIS的key
     */
    public static final String BOOK_DATA_REDIS_TYPE_KEY = "BOOK_DATA_TYPE";
    /**
     * 书本类型（女生，穿越）,存在REDIS的key
     */
    public static final String BOOK_REDIS_TYPE_KEY = "BOOK_REDIS_TYPE_KEY";

    /**
     * 书本详细信息存在Redis中的Key
     */
    public static final String BOOK_INFO_REDIS_KEY = "BOOK_INFO_REDIS_KEY";

    /**
     * 操作Book的Redis锁
     */
    public static final String HANDLER_BOOK_REDIS_LOCK = "HANDLER_BOOK_REDIS_LOCK";

    /**
     * 首页数据存在Redis的Key
     */
    public static final String XG_INDEX_REDIS_BOOK_KEY = "XG_INDEX_REDIS_BOOK_KEY_";

    /**
     * 星光首页的锁
     */
    public static final String XG_INDEX_DATA_LOCK = "XG_INDEX_DATA_LOCK";

    /**
     * 书本记录存在Redis的锁
     */
    public static final String BOOK_PAST_REDIS_LOCK = "BOOK_PAST_REDIS_LOCK";
}
