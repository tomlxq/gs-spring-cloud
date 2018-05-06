# 第六篇: 分布式配置中心(Spring Cloud Config)
## 简介
在分布式系统中，由于服务数量巨多，为了方便服务配置文件统一管理，实时更新，所以需要分布式配置中心组件。在Spring Cloud中，有分布式配置中心组件spring cloud config ，它支持配置服务放在配置服务的内存中（即本地），也支持放在远程Git仓库中。在spring cloud config 组件中，分两个角色，一是config server，二是config client。

随着我们的分布式项目越来越大，我们可能需要将配置文件抽取出来单独管理，Spring Cloud Config对这种需求提供了支持。Spring Cloud Config为分布式系统中的外部配置提供服务器和客户端支持。我们可以使用Config Server在所有环境中管理应用程序的外部属性，Config Server也称为分布式配置中心，本质上它就是一个独立的微服务应用，用来连接配置仓库并将获取到的配置信息提供给客户端使用；客户端就是我们的各个微服务应用，我们在客户端上指定配置中心的位置，客户端在启动的时候就会自动去从配置中心获取和加载配置信息。Spring Cloud Config可以与任何语言运行的应用程序一起使用。服务器存储后端的默认实现使用git，因此它轻松支持配置信息的版本管理，当然我们也可以使用Git客户端工具来管理配置信息。本文我们就先来看下Spring Cloud Config的一个基本使用。
## 构建Config Server
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
然后在application.yml中配置一下git仓库的信息，为了简单，我这里就不自己搭建git服务端了，直接使用GitHub（当然也可以使用码云），
这里需要我首先在我的Github上创建一个名为gs-spring-cloud-config的项目，创建好之后，再做如下配置：
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
然后在application.properties中配置一下git仓库的信息，为了简单，我这里就不自己搭建git服务端了，直接使用GitHub（当然也可以使用码云），这里需要我首先在我的Github上创建一个名为scConfig的项目，创建好之后，再做如下配置：
* spring.cloud.config.server.git.uri：配置git仓库地址
* spring.cloud.config.server.git.searchPaths：配置仓库路径,表示仓库下的子目录
* spring.cloud.config.label：配置仓库的分支
* spring.cloud.config.server.git.username：访问git仓库的用户名
* spring.cloud.config.server.git.password：访问git仓库的用户密码

做好这些之后我们的配置中心服务端就创建好了。

如果Git仓库为公开仓库，可以不填写用户名和密码，如果是私有仓库需要填写
## 构建配置仓库
接下来我们需要在github上设置好配置中心，首先我在本地找一个空文件夹，在该文件夹中创建一个文件夹叫gs-spring-cloud-config/respo，
然后在gs-spring-cloud-config/respo中创建四个配置文件，如下：
```
config-client.properties
config-client-dev.properties
config-client-pro.properties
```
文件respo/config-client-dev.properties文件中有一个属性：

`foo = foo version 3 from dev`

文件respo/config-client-pro.properties文件中有一个属性：

`foo = foo version 3 from pro`

然后回到respo目录下，依次执行如下命令将本地文件同步到Github仓库中，如下：
```
$ git init
$ git add respo/
$ git commit -m "add repo"
$ git remote add origin git@github.com:tomlxq/gs-spring-cloud-config.git
$ git push -u origin master
```
如此之后，我们的配置文件就上传到GitHub上了。此时启动我们的配置中心，通过/{application}/{profile}/{label}就能访问到我们的配置文件了，
其中application表示配置文件的名字，对应我们上面的配置文件就是config-client，profile表示环境，我们有dev、prod还有默认，label表示分支
http请求地址和资源文件映射如下:

* /{application}/{profile}[/{label}]
* /{application}-{profile}.yml
* /{label}/{application}-{profile}.yml
* /{application}-{profile}.properties
* /{label}/{application}-{profile}.properties

默认我们都是放在master分支上，我们在浏览器上访问结果如下：
http://localhost:8888/config-client/prod/master
```
{
    "name": "config-client",
    "profiles": [
        "prod"
    ],
    "label": "master",
    "version": "56dd1f4b8d97f675ec36e02377d3a395d98348be",
    "state": null,
    "propertySources": [
        {
            "name": "https://github.com/tomlxq/gs-spring-cloud-config/respo/config-client.properties",
            "source": {
                "foo": "foo version 3 from pro"
            }
        }
    ]
}
```
http://localhost:8888/config-client/dev/master
```
{
    "name": "config-client",
    "profiles": [
        "dev"
    ],
    "label": "master",
    "version": "56dd1f4b8d97f675ec36e02377d3a395d98348be",
    "state": null,
    "propertySources": [
        {
            "name": "https://github.com/tomlxq/gs-spring-cloud-config/respo/config-client-dev.properties",
            "source": {
                "foo": "foo version 6 from dev"
            }
        },
        {
            "name": "https://github.com/tomlxq/gs-spring-cloud-config/respo/config-client.properties",
            "source": {
                "foo": "foo version 3 from pro"
            }
        }
    ]
}
```
证明配置服务中心可以从远程程序获取配置信息。
从这里我们看到了我们放在仓库中的配置文件。
JSON中的name表示配置文件名application的部分，profiles表示环境部分，label表示分支，多了一个version，实际上就是我们GitHub上提交信息时产生的版本号，当我们访问成功后，我们还可以看到控制台打印了如下日志：
```
2018-05-06 08:03:08.899  INFO 23940 --- [nio-8888-exec-5] o.s.c.c.s.e.NativeEnvironmentRepository  : Adding property source: file:/C:/Users/tom/AppData/Local/Temp/config-repo-7270377886697097555/respo/config-client-dev.properties
2018-05-06 08:03:08.899  INFO 23940 --- [nio-8888-exec-5] o.s.c.c.s.e.NativeEnvironmentRepository  : Adding property source: file:/C:/Users/tom/AppData/Local/Temp/config-repo-7270377886697097555/respo/config-client.properties
```
实际上是配置中心通过git clone命令将配置文件在本地保存了一份，这样可以确保在git仓库挂掉的时候我们的应用还可以继续运行，
此时我们断掉网络，再访问http://localhost:8888/config-client/dev/master，一样还可以拿到数据，此时的数据就是从本地获取的。 
```
2018-05-06 08:11:58.118  WARN 23940 --- [nio-8888-exec-6] .c.s.e.MultipleJGitEnvironmentRepository : Could not fetch remote for master remote: https://github.com/tomlxq/gs-spring-cloud-config
2018-05-06 08:11:58.297  INFO 23940 --- [nio-8888-exec-6] s.c.a.AnnotationConfigApplicationContext : Refreshing org.springframework.context.annotation.AnnotationConfigApplicationContext@52147085: startup date [Sun May 06 08:11:58 CST 2018]; root of context hierarchy
2018-05-06 08:11:58.299  INFO 23940 --- [nio-8888-exec-6] f.a.AutowiredAnnotationBeanPostProcessor : JSR-330 'javax.inject.Inject' annotation found and supported for autowiring
2018-05-06 08:11:58.303  INFO 23940 --- [nio-8888-exec-6] o.s.c.c.s.e.NativeEnvironmentRepository  : Adding property source: file:/C:/Users/tom/AppData/Local/Temp/config-repo-7270377886697097555/respo/config-client.properties
```

## 构建一个config client

重新创建一个springboot项目，取名为config-client,增加依赖:
````
dependencies {
    compile('org.springframework.boot:spring-boot-starter-web')
    compile('org.springframework.cloud:spring-cloud-starter-config')
    testCompile('org.springframework.boot:spring-boot-starter-test')
}
````
然后创建bootstrap.yml文件，来获取配置信息，注意这些信息一定要放在bootstrap.yml文件中才有效，文件内容如下：
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
    
这里的name对应了配置文件中的application部分，profile对应了profile部分，label对应了label部分，uri则表示配置中心的地址。配置完成之后创建一个测试Controller


程序的入口类，写一个API接口“／hello”，返回从配置中心读取的foo变量的值
````
@RestController
@RefreshScope
public class HelloController {
    @Value("${foo}")
    String foo;
    @RequestMapping(value = "/hello")
    public String hello(){
        return foo;
    }
}
````
我们可以直接注入值，也可以通过Environment来获取值，访问结果如下：
打开浏览器,http://localhost:8881/hello,可以看到打印出了:

`foo version 3 from dev`

修改配置

`profile: dev`

重启服务后,打开浏览器,http://localhost:8881/hello,可以看到打印出了:

`foo version 3 from pro`

这就说明，config-client从config-server获取了foo的属性，而config-server是从git仓库读取的


## git解决冲突
```
$ git log
$ git reset a2e93867a1024c146433db5ef81fbab19707af5d
$ git stash
$ git pull
$ git stash pop stash@{0}
```
## 参考资料
[spring_cloud_config](http://projects.spring.io/spring-cloud/spring-cloud.html#_spring_cloud_config)