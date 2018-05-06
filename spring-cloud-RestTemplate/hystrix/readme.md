# Spring Cloud中的断路器Hystrix
什么是微服务?举个简单的例子，我想做一个用户管理项目，里边就三个功能：用户注册、用户登录、用户详情浏览。按照传统的软件开发方式直接创建一个Web项目，分分钟就把这三个功能开发出来了，但是我现在想使用微服务+服务治理的方式来开发：首先我将这个项目拆分为四个微服务，四个微服务各建一个模块，分别是用户注册模块、用户登录模块、用户详情浏览模块和数据库操作模块，这四个模块通过内部服务治理互相调用。但是现在存在一个问题，这四个模块通过服务注册与订阅的方式互相依赖，如果一个模块出现故障会导致依赖它的模块也发生故障从而发生故障蔓延，进而导致整个服务的瘫痪。比如说这里的登录模块依赖于数据库模块，如果数据库模块发生故障，那么当登录模块去调用数据库模块的时候可能得不到响应，这个调用的线程被挂起，如果处于高并发的环境下，就会导致登录模块也崩溃。当一个系统划分的模块越多，这种故障发生的频率就会越高，对于这个问题，Spring Cloud中最重要的解决方案就是断路器，那么本文我们就来看看什么是断路器。

# 服务消费者中加入断路器
首先我们需要在服务消费者中引入hystrix，如下：
```
dependencies {
    compile('org.springframework.boot:spring-boot-starter-web')
    compile('org.springframework.cloud:spring-cloud-starter-netflix-hystrix')
    compile('org.springframework.cloud:spring-cloud-starter-netflix-eureka-client')
    testCompile('org.springframework.boot:spring-boot-starter-test')
}
```
服务消费者启动入口类
引入hystrix之后，我们需要在入口类上通过@EnableCircuitBreaker开启断路器功能，如下:
```
@EnableCircuitBreaker
@SpringBootApplication
@EnableDiscoveryClient
public class HystrixApplication {

    public static void main(String[] args) {
        SpringApplication.run(HystrixApplication.class, args);
    }
    @LoadBalanced
    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```
然后我们创建一个HelloService类，如下：
```
@Service
public class HelloService {
    @Autowired
    private RestTemplate restTemplate;

    @HystrixCommand(fallbackMethod = "error")
    public String hello() {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity("http://SERVICE-HELLO/hello?name={1}", String.class,"tom");
        return responseEntity.getBody();
    }

    public String error() {
        return "error";
    }
}
```
关于这个HelloService类我说如下几点：

1. RestTemplate执行网络请求的操作我们放在HelloService中来完成。 
2. error方法是一个请求失败时回调的方法。 
3. 在hello方法上通过@HystrixCommand注解来指定请求失败时回调的方法。

将ConsumerController的逻辑修改成下面这样：
```
@RestController
public class ConsumerController {
    @Autowired
    private HelloService helloService;
    @RequestMapping(value = "/ribbon-consumer",method = RequestMethod.GET)
    public String helloController() {
        return helloService.hello();
    }
}
```

事实上，不仅仅是服务提供者被关闭时我们需要断路器，如果请求超时也会触发熔断请求，调用回调方法返回数据。

