package org.king2.sl.data.service.impl;

import com.nb304.lock.dfs_redis_lock.service.Lock;
import lombok.SneakyThrows;
import org.apache.ibatis.annotations.Param;
import org.king2.sl.common.key.BookCommandKey;
import org.king2.sl.common.key.UserCommandKey;
import org.king2.sl.common.pojo.SlBook;
import org.king2.sl.common.pojo.SlUserTable;
import org.king2.sl.common.utils.CookieUtils;
import org.king2.sl.common.utils.JsonUtils;
import org.king2.sl.common.utils.SystemResult;
import org.king2.sl.data.appoint.AddBookManageAppoint;
import org.king2.sl.data.dto.StarLightIndexDto;
import org.king2.sl.data.mapper.LocalSlBookMapper;
import org.king2.sl.data.service.StarLightIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisCluster;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 星光首页的默认实现类
 */
@Service("defaultStarLightIndexService")
public class StarLightIndexServiceImpl implements StarLightIndexService {

    @Autowired
    private Lock lock;

    /**
     * 注入RedisCluster
     */
    @Autowired
    private JedisCluster jedisCluster;

    @Autowired
    private AddBookManageAppoint addBookManageAppoint;

    @Autowired
    private LocalSlBookMapper localSlBookMapper;


    @SneakyThrows
    @Override
    public SystemResult index(HttpServletRequest request, HttpServletResponse response) {


        /**
         * 星光首页需要干的事情就特别的多了
         */

        // 获取用户的信息
        SlUserTable userInfo = (SlUserTable) request.getAttribute(UserCommandKey.USER_LOGIN_SUCCESS_POJO_INFO_REQ_KEY);

        // 创建返回的Dto
        StarLightIndexDto starLightIndexDto = new StarLightIndexDto();

        // 判断用户是否登录成功，如果登录成功那么就需要干一些其他事情
        checkUserIsLoginSuccess(starLightIndexDto, userInfo, request, response);

        // 获取到公共的数据
        getCommonData(starLightIndexDto, request);

        return new SystemResult(starLightIndexDto);
    }

    /**
     * 获取到公共的Data数据
     *
     * @param starLightIndexDto
     */
    private void getCommonData(StarLightIndexDto starLightIndexDto, HttpServletRequest re) throws Exception {

        //  查询出星光数据的所有类型（小说、漫画）
        addBookManageAppoint.getBookDataType(re);

        // 查询首页书刊
        getIndexBooks(starLightIndexDto, BookCommandKey.XG_INDEX_REDIS_BOOK_KEY + 2, 2);

    }


    /**
     * 查询首页的漫画书刊
     *
     * @param starLightIndexDto
     */
    public void getIndexBooks(StarLightIndexDto starLightIndexDto, String redisKey, Integer typeId) {

        lock.lock(BookCommandKey.XG_INDEX_DATA_LOCK, 20000);
        try {
            // 查询Redis中是否存在数据
            String redisValue = jedisCluster.get(redisKey);
            if (StringUtils.isEmpty(redisValue)) {
                // Redis中并没有数据
                // 从数据库中查询
                List<SlBook> bookInfoByTypeAndLike = localSlBookMapper.getBookInfoByTypeAndLike(typeId);
                starLightIndexDto.setIndexBooks(bookInfoByTypeAndLike);
                // 重新存入Redis
                if (!CollectionUtils.isEmpty(bookInfoByTypeAndLike)) {
                    jedisCluster.set(redisKey, JsonUtils.objectToJson(bookInfoByTypeAndLike));
                }
            } else {
                starLightIndexDto.setIndexBooks(JsonUtils.jsonToList(redisValue, SlBook.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 系统文件路径
            String pathStr = ClassUtils.getDefaultClassLoader().getResource("").getPath().substring(1);
            try {
                lock.unlock(pathStr + "/unlock.lua", BookCommandKey.XG_INDEX_DATA_LOCK);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 判断用户是否登录成功，如果登录成功那么就需要进行其他操作
     *
     * @param userInfo 用户的信息
     * @param request  请求
     * @param response 响应
     */
    private void checkUserIsLoginSuccess(StarLightIndexDto starLightIndexDto, SlUserTable userInfo, HttpServletRequest request, HttpServletResponse response) {


        // 创建历史记录List
        List<SlBook> past = null;
        // 历史记录的str
        StringBuffer pastStr = new StringBuffer();
        // 判断是否是Redis还是Cookie
        boolean cookieFlag = false;
        if (userInfo == null) {
            // 说明用户没有登录，如果没有登录的话那么就需要从Cookie中查找历史记录
            String cookieValue = CookieUtils.getCookieValue(request, UserCommandKey.USER_PAST_COOKIE_KEY, true);
            pastStr.append(cookieValue);
            cookieFlag = true;
        } else {
            // 用户登录了，我们需要从Redis长查询历史记录，并查询这个用户的消息个数
            String redisValue = jedisCluster.get(UserCommandKey.USER_PAST_REDIS_KEY + userInfo.getSlUserId());
            pastStr.append(redisValue == null ? "" : redisValue);
            // TODO 查询用户消息的个数。
            starLightIndexDto.setMessageSize(77);
        }

        if (!StringUtils.isEmpty(pastStr.toString())) {
            try {
                past = JsonUtils.jsonToList(pastStr.toString(), SlBook.class);
            } catch (Exception e) {
                if (cookieFlag) {
                    // 删除Cookie中的信息
                    CookieUtils.deleteCookie(request, response, UserCommandKey.USER_PAST_COOKIE_KEY);
                } else {
                    jedisCluster.del(UserCommandKey.USER_PAST_REDIS_KEY + userInfo.getSlUserId());
                }
                e.printStackTrace();
            }

        }

        starLightIndexDto.setSlBooks(past);
    }
}
