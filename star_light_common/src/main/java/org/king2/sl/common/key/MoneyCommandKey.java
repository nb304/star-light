package org.king2.sl.common.key;

/**
 * 金钱操作的Key
 */
public class MoneyCommandKey {

    /**
     * 金钱类型存在Redis当中的缓存Key
     */
    // TODO 如果更新了类型需要同步Redis中的信息
    public static final String MONEY_TYPE_REDIS_CACHE_KEY = "MONEY_TYPE_REDIS_CACHE_KEY";

    /**
     * 该用户的余额存在Redis中的key
     * MONEY_TOTAL_REDIS_KEY_#{userId} => MONEY_TOTAL_SIZE
     * TODO 用户操作了余额需要刷新Redis中的数据
     */
    public static final String MONEY_TOTAL_REDIS_KEY = "MONEY_TOTAL_REDIS_KEY";

    /**
     * 该用户最近九次的充值记录存在Redis中的Key
     * MONEY_LATE_NINE_REDIS_KEY_#{userId} => List<Data> ;
     * TODO 用户充值了以后需要更新数据
     */
    public static final String MONEY_LATE_NINE_REDIS_KEY = "MONEY_LATE_NINE_REDIS_KEY";

    /**
     * 金额类型Type的锁
     */
    public static final String MONEY_TYPE_LOCK = "MONEY_TYPE_LOCK";

    /**
     * 初始化订单锁
     */
    public static final String MONEY_ORDER_ID_LOCK = "MONEY_ORDER_ID_LOCK";

    /**
     * 金额锁，MONEY_LOCK+userId
     */
    public static final String MONEY_LOCK = "MONEY_LOCK";

}