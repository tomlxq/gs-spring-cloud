package com.example.controller ;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HelloController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Value("${server.port}")
    String port;
    @Autowired
    private DiscoveryClient client;

    @RequestMapping("/hello")
    public String hello(@RequestParam String name) {
        List<ServiceInstance> instances = client.getInstances("service-hello");
        for (int i = 0; i < instances.size(); i++) {
            logger.info("/hello,host:" + instances.get(i).getHost() + ",service_id:" + instances.get(i).getServiceId());
        }
        return "hello " + name + ",i am from port:" + port;
    }
}
