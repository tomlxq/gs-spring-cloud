package com.example.service;

import com.example.client.IHelloService;
import org.springframework.stereotype.Component;

@Component
public class HelloHystrixErr implements IHelloService {
    @Override
    public String hello(String name) {
         return "sorry "+name;
    }
}
