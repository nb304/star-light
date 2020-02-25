package org.king2.sl.data.service.impl;

import org.king2.sl.common.exceptions.CheckValueException;
import org.king2.sl.common.key.BookCommandKey;
import org.king2.sl.common.key.UserCommandKey;
import org.king2.sl.common.pojo.SlBook;
import org.king2.sl.common.pojo.SlBookCorrData;
import org.king2.sl.common.pojo.SlUserTable;
import org.king2.sl.common.utils.AESUtil;
import org.king2.sl.common.utils.JsonUtils;
import org.king2.sl.common.utils.MinioUtil;
import org.king2.sl.common.utils.SystemResult;
import org.king2.sl.data.mapper.LocalSlBookMapper;
import org.king2.sl.data.service.AddBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisCluster;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 默认的添加书本
 */
@Service("defaultAddBookService")
public class AddBookServiceImpl implements AddBookService {

    /**
     * 注入本地BookMapper
     */
    @Autowired
    private LocalSlBookMapper localSlBookMapper;

    @Autowired
    private JedisCluster jedisCluster;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SystemResult addBook(String content, String image, HttpServletRequest request, MinioUtil util, String typeId) throws Exception {

        try {
            /**
             * 上传文件成功，我们需要干以下的事情
             * 1、将content转换成Book对象并添加到数据库返回主键。
             * 2、将Book和BookType对应到数据库中的信息中。
             */
            Integer bookId = addBookGotoSQLAndReturnBookId(content, image, request, util);

            // 上传Book和BookType到对应的数据中当中
            addBookAndBookTypeGotoSQL(bookId, typeId);
            // 删除Redis中的书本信息，以防前面缓存穿透造成的数据丢失
            jedisCluster.del(BookCommandKey.BOOK_INFO_REDIS_KEY + bookId);
            return new SystemResult(bookId);
        } catch (Exception e) {
            // 删除图片
            util.delFile(image.substring(image.lastIndexOf("/") + 1));
            if (!(e instanceof CheckValueException)) {
                e.printStackTrace();
            }

            throw e;
        }
    }

    /**
     * 上传Book和BookType到对应的数据中当中
     *
     * @param bookId
     * @param typeId
     */
    public void addBookAndBookTypeGotoSQL(Integer bookId, String typeId) throws CheckValueException {

        if (bookId == null || bookId == 0) {
            throw new CheckValueException("添加失败，请重试。");
        }

        List<SlBookCorrData> bookCorrData = new ArrayList<>();
        for (String s : typeId.split(",")) {
            if (!s.matches("[0-9]{1,}")) {
                throw new CheckValueException("添加失败，请重试。");
            }
            SlBookCorrData data = new SlBookCorrData();
            data.setSlDataId(Integer.parseInt(s));
            data.setSlBookId(bookId);
            bookCorrData.add(data);
        }
        // 批量插入类型
        localSlBookMapper.batchBookCorrDataInsert(bookCorrData);
    }

    /**
     * 添加Book到数据并返回MySQL的主键
     *
     * @param content
     * @return
     */
    public Integer addBookGotoSQLAndReturnBookId(String content, String image, HttpServletRequest request,
                                                 MinioUtil util) throws Exception {

        // 将content转化成Book对象
        SlBook slBook = null;
        try {
            slBook = JsonUtils.jsonToPojo(AESUtil.aesDecrypt(content), SlBook.class);
            // 填写表单
            slBook.setSlCoverImage(image);
            slBook.setSlUpdateTime(new Date());
            // 添加数据到数据库当中
            SlUserTable attribute = (SlUserTable) request.getAttribute(UserCommandKey.USER_LOGIN_SUCCESS_POJO_INFO_REQ_KEY);
            slBook.setUserId(attribute.getSlUserId());
            // 校验信息是否正确
            checkBookInfo(slBook);
        } catch (Exception e) {
            throw e;
        }
        try {
            localSlBookMapper.insert(slBook);
        } catch (Exception e) {
            throw e;
        }
        return slBook.getSlBookId();
    }

    /**
     * 校验Book信息
     *
     * @param slBook
     */
    public void checkBookInfo(SlBook slBook) throws CheckValueException {

        if (StringUtils.isEmpty(slBook.getSlBookName()) || slBook.getSlBookName().length() > 50) {
            throw new CheckValueException("数据名称不能为空，且不能超过50字符");
        } else if (StringUtils.isEmpty(slBook.getSlDataTypeId() + "")) {
            throw new CheckValueException("请选择数据类型");
        } else if (!StringUtils.isEmpty(slBook.getSlContent()) && slBook.getSlContent().length() > 200) {
            throw new CheckValueException("内容最多是200字符");
        } else if (slBook.getSlDataTypeId() == null || slBook.getSlDataTypeId() == 0) {
            throw new CheckValueException("请选择数据类型");
        }

    }
}
