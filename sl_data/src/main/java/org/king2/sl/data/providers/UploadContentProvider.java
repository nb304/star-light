package org.king2.sl.data.providers;

import org.king2.sl.common.exceptions.CheckValueException;
import org.king2.sl.common.pojo.SlBook;
import org.king2.sl.common.pojo.SlBookDe;
import org.king2.sl.common.utils.MD5Utils;
import org.king2.sl.common.utils.MinioUtil;
import org.king2.sl.common.utils.SystemResult;
import org.king2.sl.data.cache.UploadContentCache;
import org.king2.sl.data.controller.UploadManageController;
import org.king2.sl.data.dto.BookContentProviderDto;
import org.king2.sl.data.mapper.LocalSlBookMapper;
import org.king2.sl.data.pool.UploadTaskPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 上传内容的消费者
 */
@Component
public class UploadContentProvider {

    @Autowired
    private LocalSlBookMapper localSlBookMapper;

    /**
     * MINIO操作对象
     */
    private static MinioUtil MINIO_UTIL = null;


    @PostConstruct
    @Transactional(rollbackFor = Exception.class)
    public void provider() {
        new Thread(() -> {
            while (true) {
                // 校验数据是否为空，如果为空就休眠
                while (!checkDataIsEmpty(3)) ;

                // 如果有资源那么就需要消费
                // 加锁
                UploadContentCache.LOCK.writeLock().lock();
                try {
                    // 缓存数据
                    Map<String, List<BookContentProviderDto>> cache = UploadContentCache.getInstance().getCache();
                    if (!cache.isEmpty()) {
                        for (Map.Entry<String, List<BookContentProviderDto>> data : cache.entrySet()) {
                            // 开始处理数据
                            try {
                                processorCacheContentData(data.getKey(), data.getValue());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                        cache.clear();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    UploadContentCache.LOCK.writeLock().unlock();
                }
            }
        }).start();
    }

    /**
     * 开始处理缓存的数据
     *
     * @param key   bookId
     * @param value 本次的内容信息
     */
    private void processorCacheContentData(String key, List<BookContentProviderDto> value) throws Exception {

        // 获取到Book的信息
        SlBook book = localSlBookMapper.getBookById(Integer.parseInt(key));
        if (book == null) {
            UploadContentCache.getInstance().getCache().remove(key);
            // TODO 给用户发出消息，因为Book为空需要通知用户
            send(CollectionUtils.isEmpty(value) ? null : value.get(0).getSlUserTable().getSlUserId());
            return;
        }

        // 定义需要回滚的文件名称
        List<String> rollBackFileName = new ArrayList<>();
        // 定义需要添加到数据库的内容集合
        List<SlBookDe> bookDes = new ArrayList<>();


        for (BookContentProviderDto bookContentProviderDto : value) {

            // 将文件上传到远程服务器
            String fileName = uploadFile(bookContentProviderDto, rollBackFileName);
            // 初始化内容的数据信息
            initContentData(fileName, book, bookContentProviderDto, bookDes, rollBackFileName);
        }

        //  初始化成功，开始上传内容到SQL当中
        initSuccessAfterAddContentGotoSQL(rollBackFileName, bookDes);
        // 更新书本的最大ORDER
        addContentSuccessAfterUpdateBookOrder(book, rollBackFileName);
        // TODO 添加数据库成功，发送消息提示用户添加Book内容成功。
        send(CollectionUtils.isEmpty(value) ? null : value.get(0).getSlUserTable().getSlUserId());
    }

    /**
     * 添加内容成功以后需要将Book的Order更新一下
     *
     * @param book             需要更新的书本信息
     * @param rollBackFileName 异常后需要删除的FileName
     */
    private void addContentSuccessAfterUpdateBookOrder(SlBook book, List<String> rollBackFileName) throws Exception {
        try {
            // 更新到SQL中
            localSlBookMapper.updateOrder(book);
        } catch (Exception e) {
            // 出错后需要删除已经上传的文件信息
            for (String fName : rollBackFileName) {
                MINIO_UTIL.delFile(fName.substring(fName.lastIndexOf("/") + 1));
            }
            throw e;
        }
    }

    /**
     * 初始化内容成功，上传内容到SQL中
     *
     * @param rollBackFileName 异常后需要删除的FileName
     * @param bookDes          上传到SQL中的信息
     */
    private void initSuccessAfterAddContentGotoSQL(List<String> rollBackFileName, List<SlBookDe> bookDes) throws Exception {

        try {
            // 添加到SQL中
            localSlBookMapper.insertDe(bookDes);
        } catch (Exception e) {
            // 出错后需要删除已经上传的文件信息
            for (String fName : rollBackFileName) {
                MINIO_UTIL.delFile(fName.substring(fName.lastIndexOf("/") + 1));
            }
            throw e;
        }
    }

    /**
     * 初始化内容的数据
     *
     * @param fileName               远程的服务器名称
     * @param book                   Book内容
     * @param bookContentProviderDto 封装了本次的数据
     * @param bookDes                准备批量上传到SQL的集合
     */
    private void initContentData(String fileName, SlBook book, BookContentProviderDto bookContentProviderDto,
                                 List<SlBookDe> bookDes, List<String> rollBackFileName) throws Exception {

        try {
            SlBookDe bookDe = new SlBookDe();
            // 初始化名字
            String originalFilename = bookContentProviderDto.getFile().getOriginalFilename();
            originalFilename = (originalFilename.substring(0, originalFilename.lastIndexOf(".")));
            originalFilename = originalFilename.length() > 50 ? originalFilename.substring(0, 49) : originalFilename;
            bookDe.setSlBookDeName(originalFilename);
            bookDe.setSlBootDeBody(fileName);
            bookDe.setSlParentId(book.getSlBookId());
            bookDe.setSlCost(Integer.parseInt(bookContentProviderDto.getCost()));
            bookDe.setSlOrder(book.getSlMaxOrder());
            bookDe.setSlCreateTime(new Date());

            // 将最大值+1
            book.setSlMaxOrder(book.getSlMaxOrder() + 1);

            bookDes.add(bookDe);
        } catch (NumberFormatException e) {
            // 将图片信息删除
            for (String fName : rollBackFileName) {
                MINIO_UTIL.delFile(fName.substring(fName.lastIndexOf("/") + 1));
            }
            throw e;
        }
    }

    /**
     * 上传文件到远程服务器
     *
     * @param file
     */
    private String uploadFile(BookContentProviderDto file, List<String> rollBackFileName) throws Exception {

        String fileName = checkUploadFile(file.getFile(), file.getInputStream());
        // 创建Min IO对象
        if (MINIO_UTIL == null) {
            MINIO_UTIL = new MinioUtil(
                    UploadManageController.FILE_SERVER_URL,
                    UploadManageController.FILE_USER_NAME,
                    UploadManageController.FILE_PASS_WORD,
                    UploadManageController.FILE_BUCKET_NAME);
        }
        SystemResult uploadResult = MINIO_UTIL.uploadFile(file.getFile(), fileName);
        rollBackFileName.add(uploadResult.getData().toString());
        return uploadResult.getData().toString();
    }

    /**
     * 对上传的图片进行校验
     *
     * @param file
     * @return
     */
    public static String checkUploadFile(MultipartFile file, InputStream inputStream) throws IOException, CheckValueException {
        if (file == null) {
            throw new CheckValueException("图片不能为空");
        } else if (file.getInputStream() == null) {
            throw new CheckValueException("图片不能为空");
        }

        StringBuffer fileName = new StringBuffer();
        String fileNameFix = MD5Utils.md5("IMAGE_" + UUID.randomUUID().toString() +
                System.currentTimeMillis());

        String originalFilename = file.getOriginalFilename();
        String fileNameEnd = originalFilename.substring(originalFilename.lastIndexOf("."));
        fileName.append(fileNameFix).append(fileNameEnd);
        return fileName.toString();
    }

    /**
     * 给用户发送消息
     *
     * @param integer
     */
    private void send(Integer integer) {
    }

    /**
     * 校验数据是否为空，如果为空就休眠
     */
    public boolean checkDataIsEmpty(int time) {
        // 加锁
        UploadContentCache.LOCK.writeLock().lock();
        try {
            // 判断队列是否为空，如果为空就休眠3分钟，以免消耗系统资源
            if (UploadContentCache.getInstance().getCache().isEmpty()) {
                UploadContentCache.W_COND.await(time, TimeUnit.MINUTES);
                return false;
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            UploadContentCache.LOCK.writeLock().unlock();
        }
    }
}
