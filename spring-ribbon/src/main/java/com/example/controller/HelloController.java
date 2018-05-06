package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HelloController {

    @Autowired
    RestTemplate restTemplate;

    @RequestMapping(value = "/hello")
    public String hello(@RequestParam String name) {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity("http://service-hello/hello?name={1}", String.class,name);
        String body = responseEntity.getBody();
        HttpStatus statusCode = responseEntity.getStatusCode();
        int statusCodeValue = responseEntity.getStatusCodeValue();
        HttpHeaders headers = responseEntity.getHeaders();
        StringBuffer result = new StringBuffer();
        result.append("responseEntity.getBody()：").append(body).append("<hr>")
                .append("responseEntity.getStatusCode()：").append(statusCode).append("<hr>")
                .append("responseEntity.getStatusCodeValue()：").append(statusCodeValue).append("<hr>")
                .append("responseEntity.getHeaders()：").append(headers).append("<hr>");
        return result.toString();
        //return restTemplate.getForObject("http://service-hello/hello?name=" + name, String.class);
    }

    @RequestMapping("/hello2")
    public String hello2(@RequestParam String name) {
        Map<String, String> map = new HashMap<>();
        map.put("name", name);
        ResponseEntity<String> responseEntity = restTemplate.getForEntity("http://service-hello/hello?name={name}", String.class, map);
        return responseEntity.getBody();
    }


}