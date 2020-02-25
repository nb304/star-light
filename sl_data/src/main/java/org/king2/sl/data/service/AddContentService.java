package org.king2.sl.data.service;

import org.king2.sl.common.utils.MinioUtil;
import org.king2.sl.common.utils.SystemResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * 添加内容的Service
 */
public interface AddContentService {

    /**
     * 添加书本的详细内容
     *
     * @param bookId  这个内容属于哪个书本
     * @param file    上传的文件
     * @param request 请求
     * @param cost    星币
     * @return 添加的结果
     */
    SystemResult addContent(Integer bookId, MultipartFile file, HttpServletRequest request, String cost);

}
