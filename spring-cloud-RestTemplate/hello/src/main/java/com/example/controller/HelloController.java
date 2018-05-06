package com.example.controller;


import com.example.entity.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;


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

    @RequestMapping(value = "/getbook1", method = RequestMethod.GET)
    public Book book1() {
        return new Book("三国演义", 90, "罗贯中", "花城出版社");
    }

    @RequestMapping(value = "/getbook2", method = RequestMethod.POST)
    public Book book2(@RequestBody Book book) {
        System.out.println(book.getName());
        book.setPrice(33);
        book.setAuthor("曹雪芹");
        book.setPublisher("人民文学出版社");
        return book;
    }

    @RequestMapping(value = "/getbook3/{id}", method = RequestMethod.PUT)
    public void book3(@RequestBody Book book, @PathVariable int id) {
        logger.info("update book:" + book);
        logger.info("update by id:" + id);
    }

    @RequestMapping(value = "/getbook4/{id}", method = RequestMethod.DELETE)
    public void book4(@PathVariable int id) {
        logger.info("delete by id:" + id);
    }
}
