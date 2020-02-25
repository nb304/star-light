package org.king2.sl.sso.service;

import org.king2.sl.common.utils.SystemResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户同意本站规则Service
 */
public interface UserConsentManageService {

    /**
     * 用户同意本站规则
     *
     * @param request
     * @return
     */
    SystemResult consent(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
