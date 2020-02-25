package org.king2.sl.sso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * SSO单点登录启动类
 */
@SpringBootApplication
@EnableEurekaClient
public class SsoApp8001 {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(SsoApp8001.class);
        System.out.println();
    }
}
