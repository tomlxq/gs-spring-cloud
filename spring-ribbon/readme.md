在微服务架构中，业务都会被拆分成一个独立的服务，服务与服务的通讯是基于http restful的。Spring cloud有两种服务调用方式，一种是ribbon+restTemplate，另一种是feign。

##ribbon简介
ribbon是一个负载均衡客户端，可以很好的控制htt和tcp的一些行为。Feign默认集成了ribbon。

##建一个服务消费者
重新新建一个spring-boot工程，取名为：service-ribbon; 
在它的pom.xml文件分别引入起步依赖spring-boot-starter-web,spring-cloud-starter-netflix-eureka-client,spring-cloud-starter-netflix-ribbon


程序名称为 service-ribbon，程序端口为8764。配置文件application.yml如下
````
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
server:
  port: 8764
spring:
  application:
    name: service-ribbon
````
在工程的启动类中,通过@EnableDiscoveryClient向服务中心注册；并且向程序的ioc注入一个bean: restTemplate;并通过@LoadBalanced注解表明这个restRemplate开启负载均衡的功能。
````
@SpringBootApplication
@EnableDiscoveryClient
public class SpringRibbonApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringRibbonApplication.class, args);
    }

    @Bean
    @LoadBalanced
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
````
在controller中用调用restTemplate.getForObject 的方法
通过之前注入ioc容器的restTemplate来消费service-hi服务的“/hi”接口，在这里我们直接用的程序名替代了具体的url地址，在ribbon中它会根据服务名来选择具体的服务实例，根据服务实例在请求的时候会用具体的url替换掉服务名
````
@RestController
public class HelloController {
    @Autowired
    RestTemplate restTemplate;
    @RequestMapping(value = "/hello")
    public String hi(@RequestParam String name){
        return restTemplate.getForObject("http://service-hello/hello?name="+name,String.class);
    }
}
````

#参考资料
[spring-cloud-ribbon](http://projects.spring.io/spring-cloud/spring-cloud.html#spring-cloud-ribbon)