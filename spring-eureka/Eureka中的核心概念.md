> 本文是Spring Cloud系列的第四篇，前面三篇文章(使用Spring Cloud搭建服务注册中心、使用Spring Cloud搭建高可用服务注册中心、Spring Cloud中服务的发现与消费)我们带大家搭建了服务注册中心，向服务注册中心注册了服务，同时也发现和消费了服务。前面的文章我们是以实际代码操作为主，这篇文章我想对前面三篇文章中涉及到的一些知识点再进行详细的梳理，对于一些前面未涉及到的配置再做进一步的说明。
首先，通过前面三篇文章的学习，小伙伴们已经发现了Eureka服务治理体系中涉及到三个核心概念：服务注册中心、服务提供者以及服务消费者，本文将从这三个方面来对Eureka服务治理体系进行一个详细的说明。

## 服务提供者
Eureka服务治理体系支持跨平台，虽然我们前文使用了Spring Boot来作为服务提供者，但是对于其他技术平台只要支持Eureka通信机制，一样也是可以作为服务提供者，换句话说，服务提供者既可以是Java写的，也可以是python写的，也可以是js写的。这些服务提供者将自己注册到Eureka上，供其它应用发现然后调用，这就是我们的服务提供者，服务提供者主要有如下一些功能：

## 服务注册
服务提供者在启动的时候会通过发送REST请求将自己注册到Eureka Server上，同时还携带了自身服务的一些元数据信息。Eureka Server在接收到这个REST请求之后，将元数据信息存储在一个双层结构的Map集合中，第一层的key是服务名，第二层的key是具体服务的实例名，我们在上篇文章最后展示出来的截图中，大家也可以看出一些端倪，如下：
```
2018-05-06 14:38:52.123  INFO 18992 --- [           main] com.netflix.discovery.DiscoveryClient    : Saw local status change event StatusChangeEvent [timestamp=1525588732123, current=UP, previous=STARTING]
2018-05-06 14:38:52.124  INFO 18992 --- [nfoReplicator-0] com.netflix.discovery.DiscoveryClient    : DiscoveryClient_SERVICE-RIBBON/192.168.1.100:service-ribbon:8764: registering service...
2018-05-06 14:38:52.163  INFO 18992 --- [nfoReplicator-0] com.netflix.discovery.DiscoveryClient    : DiscoveryClient_SERVICE-RIBBON/192.168.1.100:service-ribbon:8764 - registration status: 204
```  

同时，我们在服务注册时，需要确认一下eureka.client.register-with-eureka=true配置是否正确，该值默认就为true，表示启动注册操作，如果设置为false则不会启动注册操作。

## 服务同步
再说服务同步之前，我先描述一个场景：首先我有两个服务注册中心，地址分别是http://localhost:8761和http://localhost:8762，然后我还有两个服务提供者，地址分别是http://localhost:8763和http://localhost:8765，
然后我将8763这个服务提供者注册到8761这个注册中心上去，将8765这个服务提供者注册到8762这个注册中心上去，此时我在服务消费者中如果只向8761这个注册中心去查找服务提供者，那么是不是只能获取到8763这个服务而获取不到8765这个服务？大家看下面一张图：  

  

答案是服务消费者可以获取到两个服务提供者提供的服务。虽然两个服务提供者的信息分别被两个服务注册中心所维护，但是由于服务注册中心之间也互相注册为服务，当服务提供者发送请求到一个服务注册中心时，它会将该请求转发给集群中相连的其他注册中心，从而实现注册中心之间的服务同步，通过服务同步，两个服务提供者的服务信息我们就可以通过任意一台注册中心来获取到。OK，下面我们来通过一个简单的案例来验证一下我们上面的理论：

eureka-server工程我们前面已经分别创建了application-peer1.properties和application-peer2.properties配置文件，如下：
application-peer1.properties:
```  
spring.application.name=eureka-server
server.port=1111
eureka.instance.hostname=peer1
eureka.client.register-with-eureka=true
eureka.client.service-url.defaultZone=http://peer2:1112/eureka/
```  
application-peer2.properties:
```  
spring.application.name=eureka-server
server.port=1112
eureka.instance.hostname=peer2
eureka.client.register-with-eureka=true
eureka.client.service-url.defaultZone=http://peer1:1111/eureka/
```  
然后我们通过下面两行命令来启动两个服务注册中心实例，如下：
```  
java -jar eureka-server-0.0.1-SNAPSHOT.jar --spring.profiles.active=peer1  
java -jar eureka-server-0.0.1-SNAPSHOT.jar --spring.profiles.active=peer2
```  
然后我们给provider工程也创建两个配置文件，分别为application-p1.properties和application-p2.properties，内容如下：

application-p1.properties:
```  
spring.application.name=hello-service
server.port=8080
eureka.client.service-url.defaultZone=http://peer1:1111/eureka
```  
application-p2.properties:
```  
spring.application.name=hello-service
server.port=8081
eureka.client.service-url.defaultZone=http://peer2:1112/eureka
```  
然后通过如下命令启动两个服务提供者的实例，如下：
```  
java -jar provider-0.0.1-SNAPSHOT.jar --spring.profiles.active=p1  
java -jar provider-0.0.1-SNAPSHOT.jar --spring.profiles.active=p2
```  
OK,我们将8080这个服务注册到1111这个服务注册中心上去了，将8081这个服务注册到1112这个服务注册中心上去了。当两个服务提供者都启动成功之后，我们来看看两个服务注册中心的控制面板，如下：  、

  

  

最后我们再来看看ribbon-consumer的配置文件，如下：
```  
spring.application.name=ribbon-consumer
server.port=9000
eureka.client.service-url.defaultZone=http://peer1:1111/eureka
```  
大家看到，我们运行消费端时只向1111那个服务注册中心去获取服务列表，但是在实际运行过程中8080和8081两个服务提供者都有响应我们的请求，如下图：

 

上面的日志是8081打印的，下面的日志是8080打印的，没毛病。

## 服务续约
在注册完服务之后，服务提供者会维护一个心跳来不停的告诉Eureka Server：“我还在运行”，以防止Eureka Server将该服务实例从服务列表中剔除，这个动作称之为服务续约，和服务续约相关的属性有两个，如下：
```  
eureka.instance.lease-expiration-duration-in-seconds=90  
eureka.instance.lease-renewal-interval-in-seconds=30
```  
第一个配置用来定义服务失效时间，默认为90秒，第二个用来定义服务续约的间隔时间，默认为30秒。

## 服务消费者
消费者主要是从服务注册中心获取服务列表，拿到服务提供者的列表之后，服务消费者就知道去哪里调用它所需要的服务了，我们从下面几点来进一步了解下服务消费者。

## 获取服务
当我们启动服务消费者的时候，它会发送一个REST请求给服务注册中心来获取服务注册中心上面的服务提供者列表，而Eureka Server上则会维护一份只读的服务清单来返回给客户端，这个服务清单并不是实时数据，而是一份缓存数据，默认30秒更新一次，如果想要修改清单更新的时间间隔，可以通过eureka.client.registry-fetch-interval-seconds=30来修改，单位为秒(注意这个修改是在eureka-server上来修改)。另一方面，我们的服务消费端要确保具有获取服务提供者的能力，此时要确保eureka.client.fetch-registry=true这个配置为true。

## 服务调用
服务消费者从服务注册中心拿到服务提供者列表之后，通过服务名就可以获取具体提供服务的实例名和该实例的元数据信息，客户端将根据这些信息来决定调用哪个实例，我们之前采用了Ribbon，Ribbon中默认采用轮询的方式去调用服务提供者，进而实现了客户端的负载均衡。

## 服务下线
服务提供者在运行的过程中可能会发生关闭或者重启，当服务进行正常关闭时，它会触发一个服务下线的REST请求给Eureka Server，告诉服务注册中心我要下线了，服务注册中心收到请求之后，将该服务状态置为DOWN，表示服务已下线，并将该事件传播出去，这样就可以避免服务消费者调用了一个已经下线的服务提供者了。

## 服务注册中心
服务注册中心就是Eureka提供的服务端，它提供了服务注册与发现功能。

## 失效剔除
上面我们说到了服务下线问题，正常的服务下线发生流程有一个前提那就是服务正常关闭,但是在实际运行中服务有可能没有正常关闭，比如系统故障、网络故障等原因导致服务提供者非正常下线，那么这个时候对于已经下线的服务Eureka采用了定时清除：Eureka Server在启动的时候会创建一个定时任务，每隔60秒就去将当前服务提供者列表中超过90秒还没续约的服务剔除出去，通过这种方式来避免服务消费者调用了一个无效的服务。

## 自我保护
我们在前三篇文章中给大家看的截图上，都有这样一个警告，如下图：

    

这个警告实际上就是触发了Eureka Server的自我保护机制。Eureka Server在运行期间会去统计心跳失败比例在15分钟之内是否低于85%，如果低于85%，Eureka Server会将这些实例保护起来，让这些实例不会过期，但是在保护期内如果服务刚好这个服务提供者非正常下线了，此时服务消费者就会拿到一个无效的服务实例，此时会调用失败，对于这个问题需要服务消费者端要有一些容错机制，如重试，断路器等。我们在单机测试的时候很容易满足心跳失败比例在15分钟之内低于85%，这个时候就会触发Eureka的保护机制，一旦开启了保护机制，则服务注册中心维护的服务实例就不是那么准确了，此时我们可以使用eureka.server.enable-self-preservation=false来关闭保护机制，这样可以确保注册中心中不可用的实例被及时的剔除。

OK，以上就是我们对Eureka中服务注册中心、服务提供者、服务消费者三个核心概念的一些理解，有问题欢迎留言讨论。