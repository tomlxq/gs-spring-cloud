# 第五篇: 路由网关(zuul)
> 在微服务架构中，需要几个基础的服务治理组件，包括服务注册与发现、服务消费、负载均衡、断路器、智能路由、配置管理等，由这几个基础组件相互协作，共同组建了一个简单的微服务系统。

在Spring Cloud微服务系统中，一种常见的负载均衡方式是，客户端的请求首先经过负载均衡（zuul、Ngnix），再到达服务网关（zuul集群），然后再到具体的服。服务统一注册到高可用的服务注册中心集群，服务的所有的配置文件由配置服务管理，配置服务的配置文件放在git仓库，方便开发人员随时改配置。


## Zuul简介
Zuul的主要功能是路由转发和过滤器。路由功能是微服务的一部分，比如／api/user转发到到user服务，/api/shop转发到到shop服务。zuul默认和Ribbon结合实现了负载均衡的功能。

zuul有以下功能：

* Authentication
* Insights
* Stress Testing
* Canary Testing
* Dynamic Routing
* Service Migration
* Load Shedding
* Security
* Static Response handling
* Active/Active traffic management

## 创建service-zuul工程
增加依赖
````
dependencies {
    compile('org.springframework.boot:spring-boot-starter-web')
    compile('org.springframework.cloud:spring-cloud-starter-netflix-eureka-client')
    compile('org.springframework.cloud:spring-cloud-starter-netflix-zuul')
    testCompile('org.springframework.boot:spring-boot-starter-test')
}
````
在其入口applicaton类加上注解@EnableZuulProxy，开启zuul的功能
````
@EnableZuulProxy
@EnableEurekaClient
@SpringBootApplication
public class SpringZuulApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringZuulApplication.class, args);
    }
}
````
加上配置文件application.yml加上以下的配置代码
````
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
server:
  port: 8769
spring:
  application:
    name: service-zuul
zuul:
  routes:
    api-a:
      path: /api-a/**
      serviceId: service-ribbon
    api-b:
      path: /api-b/**
      serviceId: service-feign
````
首先指定服务注册中心的地址为http://localhost:8761/eureka/，服务的端口为8769，服务名为service-zuul；
以/api-a/ 开头的请求都转发给service-ribbon服务；
以/api-b/开头的请求都转发给service-feign服务；

http://localhost:8769/api-a/hello?name=tom

`hello tom,i am from port:8762`

http://localhost:8769/api-b/hello?name=tom

`hello tom,i am from port:8762`

这说明zuul起到了路由的作用

## 服务过滤
zuul不仅只是路由，并且还能过滤，做一些安全验证。
````
@Component
public class MyFilter extends ZuulFilter {

    private static Logger logger = LoggerFactory.getLogger(MyFilter.class);

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        logger.info(String.format("%s >>> %s", request.getMethod(), request.getRequestURL().toString()));
        Object accessToken = request.getParameter("token");
        if (accessToken == null) {
            logger.warn("token is empty");
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(401);
            try {
                ctx.getResponse().getWriter().write("token is empty");
            } catch (Exception e) {
            }

            return null;
        }
        logger.info("ok");
        return null;
    }
}
````
* filterType：返回一个字符串代表过滤器的类型，在zuul中定义了四种不同生命周期的过滤器类型，具体如下： 
    * pre：路由之前
    * routing：路由之时
    * post： 路由之后
    * error：发送错误调用
* filterOrder：过滤的顺序
* shouldFilter：这里可以写逻辑判断，是否要过滤，本文true,永远过滤。
* run：过滤器的具体逻辑。可用很复杂，包括查sql，nosql去判断该请求到底有没有权限访问。

## 参考资料：
[router_and_filter_zuul](http://projects.spring.io/spring-cloud/spring-cloud.html#_router_and_filter_zuul)