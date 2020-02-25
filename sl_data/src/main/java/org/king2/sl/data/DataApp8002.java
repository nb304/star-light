package org.king2.sl.data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextListener;

/**
 * 数据模板8002
 */
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
public class DataApp8002 {

    public static void main(String[] args) {
        SpringApplication.run(DataApp8002.class);
    }

    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }
}
