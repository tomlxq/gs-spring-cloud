##创建服务注册中心
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
通过eureka.client.registerWithEureka：false和fetchRegistry：false来表明自己是一个eureka server.


##创建一个服务提供者 (eureka client)
当client向server注册时，它会提供一些元数据，例如主机和端口，URL，主页等。Eureka server 从每个client实例接收心跳消息。 如果心跳超时，则通常将该实例从注册server中删除。

通过注解@EnableEurekaClient 表明自己是一个eurekaclient.
````
@SpringBootApplication
@EnableEurekaClient
@RestController
public class SpringHelloApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringHelloApplication.class, args);
    }

    @Value("${server.port}")
    String port;

    @RequestMapping("/hi")
    public String home(@RequestParam String name) {
        return "hi " + name + ",i am from port:" + port;
    }
}
````
需要在配置文件中注明自己的服务注册中心的地址，application.yml配置文件如下：
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
这时打开 http://localhost:8762/hello?name=tom ，你会在浏览器上看到 :

hello tom,i am from port:8762
