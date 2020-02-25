package org.king2.sl.data.service;

import org.king2.sl.common.utils.SystemResult;
import org.king2.sl.data.dto.StarLightIndexDto;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 星光首页的Service
 */
public interface StarLightIndexService {

    /**
     * 星光首页的显示
     *
     * @param request  请求
     * @param response 响应
     * @return
     */
    SystemResult index(HttpServletRequest request, HttpServletResponse response);

    /**
     * 获取首页的书刊
     *
     * @param starLightIndexDto
     */
    void getIndexBooks(StarLightIndexDto starLightIndexDto, String redisKey, Integer typeId);
}
