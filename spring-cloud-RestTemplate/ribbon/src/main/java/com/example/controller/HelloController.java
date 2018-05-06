package com.example.controller;

import com.example.entity.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;



import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HelloController {
    @Autowired
    RestTemplate restTemplate;

    @RequestMapping("/hello")
    public String sayHello(@RequestParam String name) {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity("http://service-hello/hello?name={1}", String.class, name);

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
    }
    @RequestMapping("/hello2")
    public String sayHello2(@RequestParam String name) {
        Map<String, String> map = new HashMap<>();
        map.put("name", name);
        ResponseEntity<String> responseEntity = restTemplate.getForEntity("http://service-hello/hello?name={name}", String.class, map);
        return responseEntity.getBody();
    }

    @RequestMapping("/hello3")
    public String sayHello3(@RequestParam String name) {
        UriComponents uriComponents = UriComponentsBuilder.fromUriString("http://service-hello/hello?name={name}").build().expand(name).encode();
        URI uri = uriComponents.toUri();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);
        return responseEntity.getBody();
    }

    @RequestMapping("/book1")
    public Book book1() {
        ResponseEntity<Book> responseEntity = restTemplate.getForEntity("http://service-hello/getbook1", Book.class);
        return responseEntity.getBody();
    }

    @RequestMapping("/book2")
    public Book book2() {
        Book book = restTemplate.getForObject("http://service-hello/getbook1", Book.class);
        return book;
    }

    @RequestMapping("/book3")
    public Book book3() {
        Book book = new Book();
        book.setName("红楼梦");
        ResponseEntity<Book> responseEntity = restTemplate.postForEntity("http://service-hello/getbook2", book, Book.class);
        return responseEntity.getBody();
    }

    @RequestMapping("/put")
    public void put() {
        Book book = new Book();
        book.setName("红楼梦");
        restTemplate.put("http://service-hello/getbook3/{1}", book, 99);
    }

    @RequestMapping("/delete")
    public void delete() {
        restTemplate.delete("http://service-hello/getbook4/{1}", 100);
    }
}
