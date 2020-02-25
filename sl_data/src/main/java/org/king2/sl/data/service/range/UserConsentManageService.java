package org.king2.sl.data.service.range;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户同意本站规则Service
 */
@FeignClient(value = "SSO", fallback = UserConsentManageServiceFallBack.class)
public interface UserConsentManageService {

    /**
     * 用户同意本站规则
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/consent", method = RequestMethod.GET)
    String consent(@RequestParam("request") HttpServletRequest request,
                   @RequestParam("response") HttpServletResponse response) throws Exception;
}
