package com.example.controller;
import com.example.client.IHelloService;
import com.example.service.HelloService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class HelloController {
    @Autowired
    HelloService helloService;
    @Autowired
    IHelloService helloService2;
    @RequestMapping(value = "/hello")
    public String hi(@RequestParam String name){
        return helloService.hello(name);
    }
    @RequestMapping(value = "/hello2")
    public String hi2(@RequestParam String name){
        return helloService2.hello(name);
    }

}
