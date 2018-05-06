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

如何实现
服务的发现和消费实际上是两个行为，这两个行为要由不同的对象来完成：服务的发现由Eureka客户端来完成，而服务的消费由Ribbon来完成。Ribbo是一个基于HTTP和TCP的客户端负载均衡器，当我们将Ribbon和Eureka一起使用时，Ribbon会从Eureka注册中心去获取服务端列表，然后进行轮询访问以到达负载均衡的作用，服务端是否在线这些问题则交由Eureka去维护。

这里我将service-hello工程打成一个jar包，然后用命令启动，启动两个实例，方便我一会观察负载均衡的效果。

```
java -jar spring-hello-0.0.1-SNAPSHOT.jar --port server.port=8765
java -jar spring-hello-0.0.1-SNAPSHOT.jar --port server.port=8763
```
此时，在服务注册中心我们可以看到有两个服务提供者注册成功了.
如此之后，服务提供者就准备好了，接下来我们来看看服务消费者要怎么实现。
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
这里重点是添加了spring-cloud-starter-eureka和spring-cloud-starter-ribbon依赖。


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


配置启动入口类

亮明Eureka客户端身份  

1. 首先在入口类上添加@EnableDiscoveryClient注解，通过@EnableDiscoveryClient向服务中心注册,表示该应用是一个Eureka客户端应用，这样该应用就自动具备了发现服务的能力。

2. 提供RestTemplate的Bean  

RestTemplate可以帮助我们发起一个GET或者POST请求，这个我们在后文会详细解释，这里我们只需要提供一个RestTemplate  Bean就可以了，在提供Bean的同时，添加@LoadBalanced注解，表示开启客户端负载均衡。

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


创建Controller
创建一个Controller类，并向Controller类中注入RestTemplate对象，同时在Controller中提供一个名为/hello的接口，
在该接口中，我们通过刚刚注入的restTemplate来实现对HELLO-SERVICE服务提供的/hello接口进行调用。
在调用的过程中，我们的访问地址是HELLO-SERVICE，而不是一个具体的地址。在ribbon中它会根据服务名来选择具体的服务实例，根据服务实例在请求的时候会用具体的url替换掉服务名
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

Ribbon输出了当前客户端维护的HELLO-SERVICE的服务列表情况，每一个provider的位置都展示出来，Ribbon就是按照这个列表进行轮询，进而实现基于客户端的负载均衡。同时这里的日志还输出了其他信息，比如各个实例的请求总数量，第一次连接信息，上一次连接信息以及总的请求失败数量等。
## 架构
* 一个服务注册中心，eureka server,端口为8761
* service-hello工程跑了两个实例，端口分别为8762,8763，分别向服务注册中心注册
* sercvice-ribbon端口为8764,向服务注册中心注册
* 当sercvice-ribbon通过restTemplate调用service-hello的hello接口时，因为用ribbon进行了负载均衡，会轮流的调用service-hello：8762和8763 两个端口的hello接口；

## 参考资料
[spring-cloud-ribbon](http://projects.spring.io/spring-cloud/spring-cloud.html#spring-cloud-ribbon)