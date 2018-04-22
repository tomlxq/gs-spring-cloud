#json : 与前端交互的数据交换格式
个人理解上，它的特点是可以促进 web 前后端解耦，提升团队的工作效率。 同时也是跟安卓端和 iOS 端交互的工具，目前是没想出除了 json 和 XML 之外的交流形式诶（或许等以后有空闲时间会看看）。

它的另一个特点是轻量级，简洁和清晰的层次可以方便我们阅读和编写，并且减少服务器带宽占用。

#jwt (json web token)
用人话讲就是将用户的身份信息（账号名字）、其他信息（不固定，根据需要增加）在用户登陆时提取出来，并且通过加密手段加工成一串密文，在用户登陆成功时带在返回结果发送给用户。以后用户每次请求时均带上这串密文，服务器根据解析这段密文判断用户是否有权限访问相关资源，并返回相应结果。

从网上摘录了一些优点，关于 jwt 的更多资料感兴趣的读者可以自行谷歌：

相比于session，它无需保存在服务器，不占用服务器内存开销。
无状态、可拓展性强：比如有3台机器（A、B、C）组成服务器集群，若session存在机器A上，session只能保存在其中一台服务器，此时你便不能访问机器B、C，因为B、C上没有存放该Session，而使用token就能够验证用户请求合法性，并且我再加几台机器也没事，所以可拓展性好就是这个意思。
由 2 知，这样做可就支持了跨域访问。
#Spring Boot
Spring Boot 是一个用来简化 Spring 应用的搭建以及开发过程的框架。用完后会让你大呼 : "wocao! 怎么有这么方便的东西! mama 再也不用担心我不会配置 xml 配置文件了!"。

#Spring Security
这是 Spring Security 提供的一个安全权限控制框架，可以根据使用者的需要定制相关的角色身份和身份所具有的权限，完成黑名单操作、拦截无权限的操作。配合 Spring Boot 可以快速开发出一套完善的权限系统。



http://localhost:8080/login
header:
X_Auth_Token:eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJndWVzdCIsImNyZWF0ZWQiOjE1MjQzNjU2MjQ4MjQsImV4cCI6MTUyNDk3MDQyNH0.12v14-vRr7OSTOgveGluxSFF_DGv_4zNFHzedhJ4hEj9HbKZWjWGjNIz0HJ94yzq29rkVtG9sByEJHqAvsb6QQ
Content-Type:application/json

body:
username:admin
password:123456


#参考
https://www.cnblogs.com/hackyo/p/8004928.html