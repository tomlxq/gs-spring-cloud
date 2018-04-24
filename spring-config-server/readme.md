# 简介
在分布式系统中，由于服务数量巨多，为了方便服务配置文件统一管理，实时更新，所以需要分布式配置中心组件。在Spring Cloud中，有分布式配置中心组件spring cloud config ，它支持配置服务放在配置服务的内存中（即本地），也支持放在远程Git仓库中。在spring cloud config 组件中，分两个角色，一是config server，二是config client。

# 构建Config Server
创建一个spring-boot项目，取名为config-server,增加依赖:
````
dependencies {
    compile('org.springframework.boot:spring-boot-starter-web')
    compile('org.springframework.cloud:spring-cloud-config-server')
    compile('org.springframework.cloud:spring-cloud-starter-netflix-eureka-client')
    testCompile('org.springframework.boot:spring-boot-starter-test')
}
````
在程序的入口Application类加上@EnableConfigServer注解开启配置服务器的功能:
````
@SpringBootApplication
@EnableConfigServer
public class SpringConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringConfigServerApplication.class, args);
    }
}
````

需要在程序的配置文件application.yml文件配置以下:
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
````
* spring.cloud.config.server.git.uri：配置git仓库地址
* spring.cloud.config.server.git.searchPaths：配置仓库路径
* spring.cloud.config.label：配置仓库的分支
* spring.cloud.config.server.git.username：访问git仓库的用户名
* spring.cloud.config.server.git.password：访问git仓库的用户密码

如果Git仓库为公开仓库，可以不填写用户名和密码，如果是私有仓库需要填写

远程仓库https://github.com/tomlxq/gs-spring-cloud-config 中有个文件respo/config-client-dev.properties文件中有一个属性：

`foo = foo version 3 from dev`

远程仓库https://github.com/tomlxq/gs-spring-cloud-config 中有个文件respo/config-client-pro.properties文件中有一个属性：

`foo = foo version 3 from pro`

启动程序：访问http://localhost:8888/foo/dev
````
{
    "name": "foo",
    "profiles": [
        "dev"
    ],
    "label": null,
    "version": "fc5d1d1a021d6dde77ab914b93d35444efa51504",
    "state": null,
    "propertySources": []
}
````
证明配置服务中心可以从远程程序获取配置信息。

http请求地址和资源文件映射如下:

* /{application}/{profile}[/{label}]
* /{application}-{profile}.yml
* /{label}/{application}-{profile}.yml
* /{application}-{profile}.properties
* /{label}/{application}-{profile}.properties

# 构建一个config client

重新创建一个springboot项目，取名为config-client,增加依赖:
````
dependencies {
    compile('org.springframework.boot:spring-boot-starter-web')
    compile('org.springframework.cloud:spring-cloud-starter-config')
    testCompile('org.springframework.boot:spring-boot-starter-test')
}
````
其配置文件bootstrap.yml：
````
spring:
  application:
    name: config-client
  cloud:
    config:
      label: master
      profile: dev
      uri: http://localhost:8888/
server:
  port: 8881
````
* spring.cloud.config.label 指明远程仓库的分支
* spring.cloud.config.profile
    * dev开发环境配置文件
    * test测试环境
    * pro正式环境
    * spring.cloud.config.uri= http://localhost:8888/ 指明配置服务中心的网址。

程序的入口类，写一个API接口“／hello”，返回从配置中心读取的foo变量的值
````
@SpringBootApplication
@RestController
public class SpringConfigClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringConfigClientApplication.class, args);
    }
    @Value("${foo}")
    String foo;
    @RequestMapping(value = "/hello")
    public String hello(){
        return foo;
    }
}
````
打开浏览器,http://localhost:8881/hello,可以看到打印出了:

`foo version 3 from dev`

修改配置

`profile: dev`

重启服务后,打开浏览器,http://localhost:8881/hello,可以看到打印出了:

`foo version 3 from pro`

这就说明，config-client从config-server获取了foo的属性，而config-server是从git仓库读取的

# 参考资料
[spring_cloud_config](http://projects.spring.io/spring-cloud/spring-cloud.html#_spring_cloud_config)