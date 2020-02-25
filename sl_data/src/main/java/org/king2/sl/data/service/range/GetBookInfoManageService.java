package org.king2.sl.data.service.range;

import org.king2.sl.common.utils.SystemResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 获取书本信息的接口
 */
@FeignClient(value = "BOOK", fallback = GetBookInfoManageServiceFallBack.class)
public interface GetBookInfoManageService {

    @RequestMapping(value = "/book/get", method = RequestMethod.GET)
    SystemResult getBookInfoById(@RequestParam("bookId") String bookId);
}
