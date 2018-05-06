package com.example.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class HelloController {
    @Value("${foo}")
    String foo;
    @Autowired
    Environment env;
    @RequestMapping(value = "/hello")
    public String hello(){
        return foo;
    }
    @RequestMapping("/hello2")
    public String hello2() {
        return env.getProperty("foo", "未定义");
    }
}
