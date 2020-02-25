package org.king2.sl.book.server;

import org.king2.sl.common.utils.SystemResult;

/**
 * 获取书本信息的接口
 */
public interface GetBookInfoManageService {

    /**
     * 获取书本信息根据Id
     *
     * @param bookId 书本Id
     * @return 返回书本的信息
     */
    SystemResult getBookInfoById(String bookId) throws Exception;
}
