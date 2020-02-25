package org.king2.sl.data.service.range;

import org.king2.sl.common.utils.JsonUtils;
import org.king2.sl.common.utils.SystemResult;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户同意FallBack
 */
@Component
public class UserConsentManageServiceFallBack implements UserConsentManageService {

    @Override
    public String consent(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SystemResult systemResult = new SystemResult(500, "服务器暂时繁忙，请稍后重试");
        return JsonUtils.objectToJson(systemResult);
    }
}
