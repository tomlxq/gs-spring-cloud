# Feign简介
Feign是一个声明式的伪Http客户端，它使得写Http客户端变得更简单。使用Feign，只需要创建一个接口并注解。它具有可插拔的注解特性，可使用Feign 注解和JAX-RS注解。Feign支持可插拔的编码器和解码器。Feign默认集成了Ribbon，并和Eureka结合，默认实现了负载均衡的效果。

简而言之：

Feign 采用的是基于接口的注解
Feign 整合了ribbon

## 创建一个feign的服务
新建一个spring-boot工程，取名为service-feign，在它的pom文件引入Feign的起步依赖spring-cloud-starter-openfeign、Eureka的起步依赖spring-cloud-starter-netflix-eureka-client、Web的起步依赖spring-boot-starter-web
````
dependencies {
    compile('org.springframework.boot:spring-boot-starter-web')
    compile('org.springframework.cloud:spring-cloud-starter-netflix-eureka-client')
    compile('org.springframework.cloud:spring-cloud-starter-openfeign')
    testCompile('org.springframework.boot:spring-boot-starter-test')
}
````
## 在工程的配置文件application.yml文件，指定程序名为service-feign，端口号为8765
````
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
server:
  port: 8765
spring:
  application:
    name: service-feign
````
## 在程序的启动类，加上@EnableFeignClients注解开启Feign的功能：
````
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class SpringFeignApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringFeignApplication.class, args);
    }
}
````
## 定义一个feign接口，通过@FeignClient（“服务名”），来指定调用哪个服务。比如在代码中调用了service-hello服务的“/hello”接口
````
@FeignClient(value = "service-hello")
public interface HelloService {
    @RequestMapping(value = "/hello",method = RequestMethod.GET)
    String hello(@RequestParam(value = "name") String name);
}
````
在Web层的controller层，对外暴露一个”/hello”的API接口，通过上面定义的Feign客户端HelloService 来消费服务。代码如下：
````
@RestController
public class HelloController {

    @Autowired
    HelloService helloService;
    @RequestMapping(value = "/hello",method = RequestMethod.GET)
    public String hello(@RequestParam String name){
        return helloService.hello(name);
    }
}
````
访问　http://localhost:8765/hello?name=tom
# 参考资料
[spring-cloud-feign](http://projects.spring.io/spring-cloud/spring-cloud.html#spring-cloud-feign)

