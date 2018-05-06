# 服务的注册与发现（Eureka）

## 创建服务注册中心
在这里，我们需要用的的组件上Spring Cloud Netflix的Eureka ,eureka是一个服务注册和发现模块。

启动一个服务注册中心，只需要一个注解@EnableEurekaServer，这个注解需要在springboot工程的启动application类上加：
````
@SpringBootApplication
@EnableEurekaServer
public class SpringEurekaApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringEurekaApplication.class, args);
    }
}
````

每一个实例注册之后需要向注册中心发送心跳（因此可以在内存中完成），在默认情况下erureka server也是一个eureka client ,必须要指定一个 server。
application.yml
````
server:
  port: 8761

eureka:
  instance:
    hostname: localhost
  client:
    registerWithEureka: false
    fetchRegistry: false
    serviceUrl:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
````
那么关于这几行注释，我说如下几点：
1. server.port=8761表示设置该服务注册中心的端口号
2. eureka.instance.hostname=localhost表示设置该服务注册中心的hostname
3. eureka.client.register-with-eureka=false,由于我们目前创建的应用是一个服务注册中心，而不是普通的应用，默认情况下，这个应用会向注册中心（也是它自己）注册它自己，设置为false表示禁止这种默认行为
4. eureka.client.fetch-registry=false,表示不去检索其他的服务，因为服务注册中心本身的职责就是维护服务实例，它也不需要去检索其他服务

## 创建一个服务提供者 (eureka client)
当client向server注册时，它会提供一些元数据，例如主机和端口，URL，主页等。Eureka server 从每个client实例接收心跳消息。 如果心跳超时，则通常将该实例从注册server中删除。

通过注解@EnableEurekaClient 表明自己是一个eurekaclient.

SpringHelloApplication.java
````
@SpringBootApplication
@EnableEurekaClient
public class SpringHelloApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringHelloApplication.class, args);
    }
}
````
在Spring Boot的入口函数处，通过添加@EnableDiscoveryClient注解来激活Eureka中的DiscoveryClient实现
HelloController.java
````
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
````
这里创建服务之后，在日志中将服务相关的信息打印出来。

配置服务名称和注册中心地址，application.yml配置文件如下：
````
eureka:
 client:
  serviceUrl:
   defaultZone: http://localhost:8761/eureka/
server:
 port: 8762
spring:
 application:
  name: service-hello
````

需要指明spring.application.name,这个很重要，这在以后的服务与服务之间相互调用一般都是根据这个name 。

这时打开 http://localhost:8762/hello?name=tom ，你会在浏览器上看到 :

`hello tom,i am from port:8762`

## 参考资料
[springcloud eureka server 官方文档](http://projects.spring.io/spring-cloud/spring-cloud.html#spring-cloud-eureka-server)
[springcloud eureka client 官方文档](http://projects.spring.io/spring-cloud/spring-cloud.html#_service_discovery_eureka_clients)
