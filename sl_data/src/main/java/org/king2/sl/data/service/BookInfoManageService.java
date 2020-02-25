package org.king2.sl.data.service;

import org.king2.sl.common.utils.SystemResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Book信息管理Service
 */
public interface BookInfoManageService {

    /**
     * 初始化Book的Info信息
     *
     * @param request
     * @param response
     * @param bookId
     */
    SystemResult initBookInfo(HttpServletRequest request, HttpServletResponse response, Integer bookId) throws Exception;
}
