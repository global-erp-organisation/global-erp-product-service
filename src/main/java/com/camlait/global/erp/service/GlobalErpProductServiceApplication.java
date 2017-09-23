package com.camlait.global.erp.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
//@PropertySource("classpath:application-${GLOBAL_ENV}.properties")
public class GlobalErpProductServiceApplication {

    public static void main(String... args) {
        SpringApplication.run(GlobalErpProductServiceApplication.class, args);
    }
}
