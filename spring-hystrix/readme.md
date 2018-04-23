在微服务架构中，根据业务来拆分成一个个的服务，服务与服务之间可以相互调用（RPC），在Spring Cloud可以用RestTemplate+Ribbon和Feign来调用。为了保证其高可用，单个服务通常会集群部署。由于网络原因或者自身的原因，服务并不能保证100%可用，如果单个服务出现问题，调用这个服务就会出现线程阻塞，此时若有大量的请求涌入，Servlet容器的线程资源会被消耗完毕，导致服务瘫痪。服务与服务之间的依赖性，故障会传播，会对整个微服务系统造成灾难性的严重后果，这就是服务故障的“雪崩”效应。

为了解决这个问题，业界提出了断路器模型。

## 断路器简介
Netflix开源了Hystrix组件，实现了断路器模式，SpringCloud对这一组件进行了整合。 在微服务架构中，一个请求需要调用多个服务是非常常见的
较底层的服务如果出现故障，会导致连锁故障。当对特定的服务的调用的不可用达到一个阀值（Hystric 是5秒20次） 断路器将会被打开。
断路打开后，可用避免连锁故障，fallback方法可以直接返回一个固定值。

## restTemplate中使用断路器

首先加入spring-cloud-starter-netflix-hystrix的起步依赖

````
dependencies {
    compile('org.springframework.boot:spring-boot-starter-web')
    compile('org.springframework.cloud:spring-cloud-starter-netflix-eureka-client')
    compile('org.springframework.cloud:spring-cloud-starter-netflix-hystrix')
    testCompile('org.springframework.boot:spring-boot-starter-test')
}
````

在程序的启动类SpringHystrixApplication加@EnableHystrix注解开启Hystrix
````
@SpringBootApplication
@EnableDiscoveryClient
@EnableHystrix
public class SpringHystrixApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringHystrixApplication.class, args);
    }
    @Bean
    @LoadBalanced
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
````

改造HelloService类，在hello方法上加上@HystrixCommand注解。该注解对该方法创建了熔断器的功能，并指定了fallbackMethod熔断方法，熔断方法直接返回了一个字符串
````
@Service
public class HelloService {
    @Autowired
    RestTemplate restTemplate;
    @HystrixCommand(fallbackMethod = "hiError")
    public String hello(String name){
        return restTemplate.getForObject("http://service-hello/hello?name="+name,String.class);
    }
    public String hiError(String name) {
        return "hi,"+name+",sorry,error!";
    }
}
````

当 service-hello 工程不可用的时候，service-hystrix调用 service-hello的API接口时，会执行快速失败，直接返回一组字符串，而不是等待响应超时，这很好的控制了容器的线程阻塞。

## Feign中使用断路器

Feign是自带断路器的，在D版本的Spring Cloud中，它没有默认打开。需要在配置文件中配置打开它，在配置文件加以下代码：
````
spring:
  application:
    name: service-feign
````
只需要在FeignClient的IHelloService接口的注解中加上fallback的指定类就行了：
````
@FeignClient(value = "service-hello",fallback = HelloHystrixErr.class)
public interface IHelloService {
    @RequestMapping(value = "/hello",method = RequestMethod.GET)
    String hello(@RequestParam(value = "name") String name);
}
````
HelloHystrixErr需要实现IHelloService 接口，并注入到Ioc容器中
````
@Component
public class HelloHystrixErr implements IHelloService {
    @Override
    public String hello(String name) {
         return "sorry "+name;
    }
}
````

# 参考资料
* [circuit_breaker_hystrix](http://projects.spring.io/spring-cloud/spring-cloud.html#_circuit_breaker_hystrix_clients)
* [feign-hystrix](http://projects.spring.io/spring-cloud/spring-cloud.html#spring-cloud-feign-hystrix)
* [hystrix_dashboard](http://projects.spring.io/spring-cloud/spring-cloud.html#_circuit_breaker_hystrix_dashboard)


