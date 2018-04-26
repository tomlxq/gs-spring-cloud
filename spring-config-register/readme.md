上一篇文章讲述了一个服务如何从配置中心读取文件，配置中心如何从远程git读取配置文件，当服务实例很多时，都从配置中心读取文件，这时可以考虑将配置中心做成一个微服务，将其集群化，从而达到高可用
# 准备工作
继续使用上一篇文章的工程，创建一个eureka-server工程，用作服务注册中心。

在其build.gradle文件引入Eureka的起步依赖spring-cloud-starter-netflix-eureka-server，代码如下:
````
dependencies {
    compile('org.springframework.cloud:spring-cloud-starter-netflix-eureka-server')
    testCompile('org.springframework.boot:spring-boot-starter-test')
}
````

在配置文件application.yml上，指定服务端口为8889，加上作为服务注册中心的基本配置，代码如下：
````
server:
  port: 8889

eureka:
  instance:
    hostname: localhost
  client:
    registerWithEureka: false
    fetchRegistry: false
    serviceUrl:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
````
入口类：
````
@EnableEurekaServer
@SpringBootApplication
public class SpringConfigRegisterApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringConfigRegisterApplication.class, args);
    }
}
````
# 改造config-server
在其build.gradle文件加上EurekaClient的起步依赖spring-cloud-starter-netflix-eureka-client，代码如下:
````
dependencies {
    compile('org.springframework.boot:spring-boot-starter-web')
    compile('org.springframework.cloud:spring-cloud-config-server')
    compile('org.springframework.cloud:spring-cloud-starter-netflix-eureka-client')
    testCompile('org.springframework.boot:spring-boot-starter-test')
}
````

配置文件application.yml，指定服务注册地址为http://localhost:8889/eureka/
````
spring:
  application:
    name: config-server
  cloud:
    config:
      server:
        git:
          uri: https://github.com/tomlxq/gs-spring-cloud-config
          searchPaths: respo
          #username: tomlxq
          #password:
      label: master
server:
  port: 8888
  
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8889/eureka/
````
最后需要在程序的启动类Application加上@EnableEureka的注解。
````
@SpringBootApplication
@EnableConfigServer
@EnableEurekaClient
public class SpringConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringConfigServerApplication.class, args);
    }
}
````
# 改造config-client
将其注册到微服务注册中心，作为Eureka客户端，需要build.gradle文件加上起步依赖spring-cloud-starter-netflix-eureka-client，代码如下：
````
dependencies {
    compile('org.springframework.boot:spring-boot-starter-web')
    compile('org.springframework.cloud:spring-cloud-starter-config')
    compile('org.springframework.cloud:spring-cloud-starter-netflix-eureka-client')
    testCompile('org.springframework.boot:spring-boot-starter-test')
}
````
配置文件bootstrap.yml，注意是bootstrap。加上服务注册地址为http://localhost:8889/eureka/
````
spring:
  application:
    name: config-client
  cloud:
    config:
      label: master
      profile: pro
      #uri: http://localhost:8888/
      discovery:
        enabled: true
        serviceId: config-server
server:
  port: 8881

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8889/eureka/
````

* spring.cloud.config.discovery.enabled 是从配置中心读取文件。
* spring.cloud.config.discovery.serviceId 配置中心的servieId，即服务名。

这时发现，在读取配置文件不再写ip地址，而是服务名，这时如果配置服务部署多份，通过负载均衡，从而高可用。

依次启动eureka-servr,config-server,config-client 
访问网址：http://localhost:8889/

访问http://localhost:8881/hello，浏览器显示：
`foo version 3 from pro`

将profile改成`profile: dev`

`foo version 3 from dev`

# 参考资料
[spring_cloud_config](http://projects.spring.io/spring-cloud/spring-cloud.html#_spring_cloud_config)