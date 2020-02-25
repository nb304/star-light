package org.king2.sl.sso.service;

/**
 * 用户激活管理Service
 */
public interface UserActiveManageService {

    /**
     * 激活用户
     *
     * @param uName
     * @throws Exception
     */
    void active(String uName, String email) throws Exception;
}
