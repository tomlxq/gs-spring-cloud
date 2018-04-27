# 第二篇: 服务消费者（rest+ribbon）
在微服务架构中，业务都会被拆分成一个独立的服务，服务与服务的通讯是基于http restful的。Spring cloud有两种服务调用方式，一种是ribbon+restTemplate，另一种是feign。

## ribbon简介

> Ribbon is a client side load balancer which gives you a lot of control over the behaviour of HTTP and TCP clients. Feign already uses Ribbon, so if you are using @FeignClient then this section also applies.
—–摘自官网

ribbon是一个负载均衡客户端，可以很好的控制htt和tcp的一些行为。Feign默认集成了ribbon。

ribbon 已经默认实现了这些配置bean：

* IClientConfig ribbonClientConfig: DefaultClientConfigImpl

* IRule ribbonRule: ZoneAvoidanceRule

* IPing ribbonPing: NoOpPing

* ServerList ribbonServerList: ConfigurationBasedServerList

* ServerListFilter ribbonServerListFilter: ZonePreferenceServerListFilter

* ILoadBalancer ribbonLoadBalancer: ZoneAwareLoadBalancer

## 建一个服务消费者
重新新建一个spring-boot工程，取名为：service-ribbon; 
在它的build.gradle文件分别引入起步依赖spring-boot-starter-web,spring-cloud-starter-netflix-eureka-client,spring-cloud-starter-netflix-ribbon
````
dependencies {
    compile('org.springframework.boot:spring-boot-starter-web')
    compile('org.springframework.cloud:spring-cloud-starter-netflix-eureka-client')
    compile('org.springframework.cloud:spring-cloud-starter-netflix-ribbon')
    testCompile('org.springframework.boot:spring-boot-starter-test')
}
````
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
通过之前注入ioc容器的restTemplate来消费service-hi服务的“/hello”接口，在这里我们直接用的程序名替代了具体的url地址，在ribbon中它会根据服务名来选择具体的服务实例，根据服务实例在请求的时候会用具体的url替换掉服务名
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
在浏览器上多次访问http://localhost:8764/hello?name=tom，浏览器交替显示：
````
hello tom,i am from port:8762
hello tom,i am from port:8763
````
## 架构
* 一个服务注册中心，eureka server,端口为8761
* service-hello工程跑了两个实例，端口分别为8762,8763，分别向服务注册中心注册
* sercvice-ribbon端口为8764,向服务注册中心注册
* 当sercvice-ribbon通过restTemplate调用service-hello的hello接口时，因为用ribbon进行了负载均衡，会轮流的调用service-hello：8762和8763 两个端口的hello接口；

## 参考资料
[spring-cloud-ribbon](http://projects.spring.io/spring-cloud/spring-cloud.html#spring-cloud-ribbon)