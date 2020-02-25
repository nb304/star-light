package org.king2.sl.data.config;

import org.king2.sl.data.interceptors.UserIsLoginInterceptor;
import org.king2.sl.data.interceptors.UserLoginInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 数据配置类
 */
@Configuration
@MapperScan("org.king2.sl.data.mapper")
public class DataConfig implements WebMvcConfigurer {

    @Bean
    public UserIsLoginInterceptor userIsLoginInterceptor() {
        return new UserIsLoginInterceptor();
    }

    @Bean
    public UserLoginInterceptor userLoginInterceptor() {
        return new UserLoginInterceptor();
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/show/add/show/consent").setViewName("StarMoneyManage/ConsentContent");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 拦截所有的请求,判断用户是否登录
        registry.addInterceptor(userIsLoginInterceptor()).addPathPatterns("/**");
        // 拦截所有的请求，如果没有登录直接放行，登录了以后需要判断一些东西。
        registry.addInterceptor(userLoginInterceptor()).addPathPatterns("/**");
    }
}
