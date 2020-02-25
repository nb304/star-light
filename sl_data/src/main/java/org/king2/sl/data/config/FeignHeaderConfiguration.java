package org.king2.sl.data.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Feign请求头处理
 */
@Configuration
public class FeignHeaderConfiguration {


    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // 获取请求头
            // TODO 开启熔断器就失效(已解决)
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                //获取到Request
                HttpServletRequest request = requestAttributes.getRequest();
                requestTemplate.header("cookie", request.getHeader("cookie"));
            }
        };
    }

}
