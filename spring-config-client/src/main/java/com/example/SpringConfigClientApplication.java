package com.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableEurekaClient
@RestController
@RefreshScope
public class SpringConfigClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringConfigClientApplication.class, args);
    }
    @Value("${foo}")
    String foo;
    @RequestMapping(value = "/hello")
    public String hello(){
        return foo;
    }
}
