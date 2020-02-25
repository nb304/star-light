package org.king2.sl.data.service.impl;

import org.king2.sl.common.annotations.Log;
import org.king2.sl.common.exceptions.CheckValueException;
import org.king2.sl.common.key.UserCommandKey;
import org.king2.sl.common.pojo.SlBook;
import org.king2.sl.common.pojo.SlUserTable;
import org.king2.sl.common.utils.SystemResult;
import org.king2.sl.data.cache.UploadContentCache;
import org.king2.sl.data.dto.BookContentProviderDto;
import org.king2.sl.data.mapper.LocalSlBookMapper;
import org.king2.sl.data.pool.UploadTaskPool;
import org.king2.sl.data.service.AddContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import redis.clients.jedis.JedisCluster;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 默认的添加内容接口
 */
@Service("defaultAddContentService")
public class AddContentServiceImpl implements AddContentService {

    @Override
    @Log
    public SystemResult addContent(Integer bookId, MultipartFile file, HttpServletRequest request, String cost) {

        try {
            InputStream inputStream = file.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 取出用户信息
        SlUserTable userTable = (SlUserTable) request.getAttribute(UserCommandKey.USER_LOGIN_SUCCESS_POJO_INFO_REQ_KEY);
        UploadTaskPool.getInstance().getPool().execute(() -> {
            /**
             * 由于这里上传可能会很久，所以我们需要顾及用户的体验，所以我们采用生产者与消费者模式
             * 1、将我们本次的文件对象通过分组存入JVM内存中
             * 2、另外一个线程负责消费这些消息
             */

            // 开启锁
            UploadContentCache.LOCK.writeLock().lock();

            if (userTable == null) {
                System.out.println("用户信息为NULL");
            }
            try {
                // 封装参数到共享内存中
                Map<String, List<BookContentProviderDto>> cache = UploadContentCache.getInstance().getCache();
                List<BookContentProviderDto> bookContentProviders = cache.get(bookId + "");
                if (CollectionUtils.isEmpty(bookContentProviders)) bookContentProviders = new ArrayList<>();
                // 开始封装
                try {
                    bookContentProviders.add(new BookContentProviderDto(file, userTable, cost, file.getInputStream()));
                } catch (Exception e) {

                }
                cache.put(bookId + "", bookContentProviders);
                // 唤醒所有睡眠的线程
                UploadContentCache.W_COND.signalAll();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                UploadContentCache.LOCK.writeLock().unlock();
            }
        });

        return new SystemResult("OK");
    }
}
