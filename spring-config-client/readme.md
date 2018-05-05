# 消息总线(Spring Cloud Bus)

Spring Cloud Bus 将分布式的节点用轻量的消息代理连接起来。它可以用于广播配置文件的更改或者服务之间的通讯，也可以用于监控。本文要讲述的是用Spring Cloud Bus实现通知微服务架构的配置文件的更改。
## 准备工作
只需要在配置文件中配置 spring-cloud-starter-bus-amqp ；这就是说我们需要装rabbitMq，点击rabbitmq下载。至于怎么使用 rabbitmq，搜索引擎下。

## 改造config-client

在build.gradle文件加上起步依赖spring-cloud-starter-bus-amqp，完整的配置文件如下：
````
dependencies {
    compile('org.springframework.boot:spring-boot-starter-web')
    compile('org.springframework.cloud:spring-cloud-starter-config')
    compile('org.springframework.cloud:spring-cloud-starter-netflix-eureka-client')
    compile('org.springframework.cloud:spring-cloud-starter-bus-amqp')
    compile('org.springframework.boot:spring-boot-starter-actuator')
    testCompile('org.springframework.boot:spring-boot-starter-test')
}
````
在配置文件bootstrap.yml中加上RabbitMq的配置，包括RabbitMq的地址、端口，用户名、密码，代码如下：

````
spring:
  application:
    name: config-client
  cloud:
    config:
      label: master
      profile: dev
      #uri: http://localhost:8888/
      discovery:
        enabled: true
        serviceId: config-server
  rabbitmq:
    host: localhost
    port: 5672
    #username:
    #password:
server:
  port: 8881

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8889/eureka/
````

如果rabbitmq有用户名密码，输入即可。

依次启动register-server、confg-server,启动两个config-client，端口为：8881、8882。

访问http://localhost:8881/hello 或者http://localhost:8882/hello 浏览器显示：
`foo version 3 from dev`

这时我们去代码仓库将foo的值改为“foo = foo version 4 from dev”，即改变配置文件foo的值。如果是传统的做法，需要重启服务，才能达到配置文件的更新。此时，我们只需要发送post请求：http://localhost:8881/bus/refresh，你会发现config-client会重新读取配置文件

`Fetching config from server at: http://192.168.1.100:8888/`
这时我们再访问http://localhost:8881/hello 或者http://localhost:8882/hello 浏览器显示：

`foo version 4 from dev`

另外，/bus/refresh接口可以指定服务，即使用”destination”参数，比如 “/bus/refresh?destination=customers:**” 即刷新服务名为customers的所有服务，不管ip。


当git文件更改的时候，通过pc端用post 向端口为8882的config-client发送请求/bus/refresh／；此时8882端口会发送一个消息，由消息总线向其他服务传递，从而使整个微服务集群都达到更新配置文件。

## SpringBoot2.0 Config客户端自动刷新时没有/bus/refresh端点
Spring boot 2.0的改动较大，/bus/refresh全部整合到actuador里面了，所以之前1.x的management.security.enabled全部失效，不适用于2.0
适用于2.0的配置是这样的：
````
 management:
  endpoints:
    web:
      exposure:
        include: bus-refresh
````        
另外注解

 `@RefreshScope `
 
需要在配置的页面加上，就是说附带@Value的页面加上此注解

请求刷新的页面由原来1.5.x的`localhost:8881/bus/refresh`
变成：`http://localhost:8881/actuator/bus-refresh`

注意：config-server和config-client的配置都得加上
````   
 management:
  endpoints:
    web:
      exposure:
        include: bus-refresh
````
启动配置中心时可以看到如下日志：
````
 s.b.a.e.w.s.WebMvcEndpointHandlerMapping : Mapped "{[/actuator/bus-refresh/{destination}],methods=[POST]}" onto public java.lang.Object org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping$OperationHandler.handle(javax.servlet.http.HttpServletRequest,java.util.Map<java.lang.String, java.lang.String>)
 s.b.a.e.w.s.WebMvcEndpointHandlerMapping : Mapped "{[/actuator/bus-refresh],methods=[POST]}" onto public java.lang.Object org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping$OperationHandler.handle(javax.servlet.http.HttpServletRequest,java.util.Map<java.lang.String, java.lang.String>)
````
* /actuator/refresh ：刷新单个节点
* /actuator/bus-refresh: 刷新所有节点

使用postman测试：
发送Post请求 http://localhost:8881/hello 和 http://localhost:8882/hello

修改配置文件：`gs-spring-cloud-config/respo/config-client-dev.properties`

发送post请求 http://localhost:8881/actuator/bus-refresh
后可以看到自动变了

##　安装rabbitMq

1. 下载地址

    http://www.rabbitmq.com/download.html
2. 运行

    输入http://localhost:15672，出下以下界面，说明安装成功

3. cloud整合配置说明

    默认端口： 5672
    默认用户名：guest
    默认密码：guest
    
4. 修改两个项目的yml文件，加入以下配置：
````
spring:
  rabbitmq: 
    host: localhost
    port: 5672
    username: guest
    password: guest
management: 
  endpoints:
    web:
      exposure: 
        include: "*"
      cors:
        allowed-origins: "*"
        allowed-methods: "*" 
````
配置说明：
rabbitmq配置
默认关闭了bus请求url，因此需要打开才能使用

## 参考资料
[spring_cloud_bus](http://projects.spring.io/spring-cloud/spring-cloud.html#_spring_cloud_bus)

