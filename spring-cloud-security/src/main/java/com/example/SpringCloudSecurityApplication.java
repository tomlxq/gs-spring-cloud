package com.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(value = {"com.example.dao"})
public class SpringCloudSecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudSecurityApplication.class, args);
    }
}
