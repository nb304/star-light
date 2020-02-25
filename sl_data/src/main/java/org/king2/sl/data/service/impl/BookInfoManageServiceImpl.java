package org.king2.sl.data.service.impl;

import com.nb304.lock.dfs_redis_lock.service.Lock;
import io.minio.messages.Upload;
import org.king2.sl.common.exceptions.CheckValueException;
import org.king2.sl.common.key.BookCommandKey;
import org.king2.sl.common.key.UserCommandKey;
import org.king2.sl.common.pojo.SlBook;
import org.king2.sl.common.pojo.SlBookDto;
import org.king2.sl.common.pojo.SlUserTable;
import org.king2.sl.common.utils.CookieUtils;
import org.king2.sl.common.utils.JsonUtils;
import org.king2.sl.common.utils.SystemResult;
import org.king2.sl.data.pool.UploadTaskPool;
import org.king2.sl.data.service.BookInfoManageService;
import org.king2.sl.data.service.range.GetBookInfoManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisCluster;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;

/**
 * 默认的书本信息Service
 */
@Service("defaultBookInfoService")
@SuppressWarnings("all")
public class BookInfoManageServiceImpl implements BookInfoManageService {

    /**
     * 注入远程的书本信息服务
     */
    @Autowired
    private GetBookInfoManageService getBookInfoManageService;

    /**
     * 注入RedisCluster依赖
     */
    @Autowired
    private JedisCluster jedisCluster;

    /**
     * 注入Redis分布式锁
     */
    @Autowired
    private Lock lock;

    @Override
    public SystemResult initBookInfo(HttpServletRequest request, HttpServletResponse response, Integer bookId) throws Exception {

        // 调用远程的获取书本信息服务
        SystemResult bookInfoById = getBookInfoManageService.getBookInfoById(bookId.toString());
        if (bookInfoById.getStatus() != 200) {
            return bookInfoById;
        }

        // 到这里说明信息查询成功，我们需要记录用户的历史记录
        UploadTaskPool.getInstance().getPool().execute(() -> {
            try {
                writePastRecord(request, response, bookId, bookInfoById.getData());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // 对书本或者其他的操作
        SlBookDto slBookDto = bookElseHandler(bookInfoById.getData());
        return new SystemResult(slBookDto);
    }

    /**
     * 书本其他的操作
     */
    private SlBookDto bookElseHandler(Object object) throws CheckValueException, IOException {


        // TODO 查询评论

        //   查询本次Book在前端使用什么容器显示
        SlBookDto book = JsonUtils.jsonToPojo(object.toString(), SlBookDto.class);
        if (book.getSlBook().getSlDataTypeId() == 1) {
            // 说明是小说类型的，如果是小说类型的话我们就需要将这个文件流给读取出来
            if (CollectionUtils.isEmpty(book.getSlBookDes())) {
                throw new CheckValueException("暂时还没有章节");
            }
            String content = getBookContent(book.getSlBookDes().get(0).getSlBootDeBody());
            book.setContent(content);
        }

        return book;
    }

    /**
     * 获取小说的内容
     *
     * @param book Book
     * @return
     */
    private String getBookContent(String fileName) throws CheckValueException {

        BufferedReader br = null;
        try {
            URL url = new URL(fileName);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setConnectTimeout(20000);
            urlConnection.setReadTimeout(20000);
            String line = "";
            StringBuffer stringBuffer = new StringBuffer();
            br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "gbk"));
            while (null != (line = br.readLine())) {
                stringBuffer.append(line);
            }
            return stringBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new CheckValueException("服务器暂时不稳定");
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 记录用户的历史记录
     *
     * @param request  请求
     * @param response 响应
     * @param bookId   书本Id
     * @param data     当前Book的所有信息类型(SlBookDto)
     */
    private void writePastRecord(HttpServletRequest request, HttpServletResponse response, Integer bookId, Object data) throws Exception {

        // 转换数据
        SlBookDto slBookDto = JsonUtils.jsonToPojo(data.toString(), SlBookDto.class);

        // 判断用户是否登录
        if ("true".equals(request.getAttribute(UserCommandKey.USER_IS_LOGIN_REQUEST_KEY).toString())) {
            // 说明登录成功，需要将值更新到Redis当中
            SlUserTable attribute = (SlUserTable) request.getAttribute(UserCommandKey.USER_LOGIN_SUCCESS_POJO_INFO_REQ_KEY);
            userLoginAndPastWriteRedis(bookId, slBookDto, attribute.getSlUserId());
        } else {
            // 说明用户没有登录，用户没有登录，我们就需要重新写入用户的Cookie当中
            userIsNotLoginAndPastWriteCookie(request, response, bookId, slBookDto);
        }
    }

    /**
     * 用户登录了就将历史记录更新到Redis
     *
     * @param bookId
     * @param slBookDto
     * @param userId    用户id
     */
    private void userLoginAndPastWriteRedis(Integer bookId, SlBookDto slBookDto, Integer userId) throws Exception {

        // 需要进行加锁
        lock.lock(BookCommandKey.BOOK_PAST_REDIS_LOCK, 10000);
        try {
            // 从Redis中获取到信息
            String redisValue = jedisCluster.get(UserCommandKey.USER_PAST_REDIS_KEY + userId);
            List<SlBook> books = new LinkedList<>();
            if (!StringUtils.isEmpty(redisValue)) {
                books = JsonUtils.jsonToList(redisValue, SlBook.class);
            }
            // 获取新的历史记录
            getNewPasts(books, userId, slBookDto.getSlBook());
            // 将值重新存入Redis
            jedisCluster.set(UserCommandKey.USER_PAST_REDIS_KEY + userId, JsonUtils.objectToJson(books));
        } finally {
            // 系统文件路径
            String pathStr = ClassUtils.getDefaultClassLoader().getResource("").getPath().substring(1);
            try {
                lock.unlock(pathStr + "/unlock.lua", BookCommandKey.BOOK_PAST_REDIS_LOCK);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 构建一个新的集合
     *
     * @param userId
     * @param slBook
     * @return
     * @throws IOException
     */
    private void getNewPasts(List<SlBook> books, Integer userId, SlBook slBook) throws IOException {

        // 记录一个需要删除的索引
        int exist = -1;

        for (int i = 0; i < books.size(); i++) {
            if ((books.get(i).getSlBookId() + "").equals(slBook.getSlBookId() + "")) {
                exist = i;
                break;
            }
        }

        // 判断是否需要删除
        if (exist >= 0) {
            books.remove(exist);
        } else if (exist == -1) {
            // 需要判断是否已经满了12个历史记录
            if (!CollectionUtils.isEmpty(books) && books.size() == 12) {
                books.remove(books.size() - 1);
            }
        }

        // 将值添加到头
        if (books.size() > 0) {
            books.add(0, slBook);
        } else {
            books.add(slBook);
        }

    }


    /**
     * 用户没有登录我们需要将历史记录写到Cookie当中
     *
     * @param request  请求
     * @param response 响应
     * @param bookId   书本Id
     * @param data     当前Book的所有信息类型(SlBookDto)
     */
    private void userIsNotLoginAndPastWriteCookie(HttpServletRequest request, HttpServletResponse response, Integer bookId, SlBookDto data) throws Exception {

        // 从Cookie中获取到信息
        String cookieValue = CookieUtils.getCookieValue(request, UserCommandKey.USER_PAST_COOKIE_KEY, true);
        List<SlBook> slBooks = new LinkedList<>();
        if (!StringUtils.isEmpty(cookieValue)) {
            slBooks = JsonUtils.jsonToList(cookieValue, SlBook.class);
        }


        // 记录是否存在的索引
        int exist = -1;
        // 判断当前BookId是否存在于List当中
        for (int i = 0; i < slBooks.size(); i++) {
            if ((slBooks.get(i).getSlBookId() + "").equals(bookId + "")) {
                // 说明存在于以前的集合当中，我们需要将这次的索引记住，在遍历完成后删除该元素。
                exist = i;
                break;
            }
        }

        // 删除这个元素
        if (exist >= 0) {
            slBooks.remove(exist);
        } else if (exist == -1) {
            // 需要判断是否已经满了12个历史记录
            if (!CollectionUtils.isEmpty(slBooks) && slBooks.size() == 12) {
                slBooks.remove(slBooks.size() - 1);
            }
        }
        // 将值添加到头
        if (slBooks.size() > 0) {
            slBooks.add(0, data.getSlBook());
        } else {
            slBooks.add(data.getSlBook());
        }

        // 重新写回Cookie
        CookieUtils.setCookie(request, response, UserCommandKey.USER_PAST_COOKIE_KEY, JsonUtils.objectToJson(slBooks), true);
    }


}
