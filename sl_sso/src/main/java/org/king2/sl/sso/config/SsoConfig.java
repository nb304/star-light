package org.king2.sl.sso.config;

import org.king2.sl.sso.interceptor.ServerIpInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sso配置类
 */
@Configuration
@MapperScan("org.king2.sl.sso.mapper")
@PropertySource("classpath:config.properties")
public class SsoConfig implements WebMvcConfigurer {

    @Bean
    public ServerIpInterceptor serverIpInterceptor() {
        return new ServerIpInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(serverIpInterceptor()).addPathPatterns("/**");
    }
}
