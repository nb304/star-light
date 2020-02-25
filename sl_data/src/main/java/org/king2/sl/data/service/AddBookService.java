package org.king2.sl.data.service;

import org.apache.ibatis.annotations.Insert;
import org.king2.sl.common.utils.MinioUtil;
import org.king2.sl.common.utils.SystemResult;

import javax.servlet.http.HttpServletRequest;

/**
 * 添加书本Service
 */
public interface AddBookService {

    /**
     * 添加书本
     *
     * @param content 加密成的内容
     * @param image   图片
     * @param request 请求
     * @param util    文件工具
     * @param typeId  数据类型
     * @return 添加的结果
     */
    SystemResult addBook(String content, String image, HttpServletRequest request, MinioUtil util, String typeId) throws Exception;

}
