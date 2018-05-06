# Spring RestTemplate中几种常见的请求方式

在Spring Cloud中服务的发现与消费一文中，当我们从服务消费端去调用服务提供者的服务的时候，使用了一个很好用的对象，叫做RestTemplate，当时我们只使用了RestTemplate中最简单的一个功能getForEntity发起了一个get请求去调用服务端的数据，同时，我们还通过配置@LoadBalanced注解开启客户端负载均衡，RestTemplate的功能不可谓不强大，那么今天我们就来详细的看一下RestTemplate中几种常见请求方法的使用。

本文主要从以下四个方面来看RestTemplate的使用：

* GET请求

* POST请求

* PUT请求

* DELETE请求 


其中commons是一个公共模块，是一个普通的JavaSE工程，我们一会主要将实体类写在这个模块中，hello和ribbon是两个spring boot项目，hello将扮演服务提供者的角色，ribbon扮演服务消费者的角色。

然后在hello和ribbon模块中添加对commons的依赖，依赖代码如下：
```
dependencies {
    ...
    compile('com.example:commons')
    ...
}
```

## GET请求
在RestTemplate中，发送一个GET请求，我们可以通过如下两种方式：

第一种：getForEntity
getForEntity方法的返回值是一个ResponseEntity<T>，ResponseEntity<T>是Spring对HTTP请求响应的封装，包括了几个重要的元素，如响应码、contentType、contentLength、响应消息体等。比如下面一个例子：
```
 @RequestMapping("/hello")
    public String sayHello(@RequestParam String name) {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity("http://service-hello/hello?name={1}", String.class, name);

        String body = responseEntity.getBody();
        HttpStatus statusCode = responseEntity.getStatusCode();
        int statusCodeValue = responseEntity.getStatusCodeValue();
        HttpHeaders headers = responseEntity.getHeaders();
        StringBuffer result = new StringBuffer();
        result.append("responseEntity.getBody()：").append(body).append("<hr>")
                .append("responseEntity.getStatusCode()：").append(statusCode).append("<hr>")
                .append("responseEntity.getStatusCodeValue()：").append(statusCodeValue).append("<hr>")
                .append("responseEntity.getHeaders()：").append(headers).append("<hr>");
        return result.toString();
    }
```
关于这段代码，我说如下几点：

* getForEntity的第一个参数为我要调用的服务的地址，这里我调用了服务提供者提供的/hello接口，注意这里是通过服务名调用而不是服务地址，如果写成服务地址就没法实现客户端负载均衡了。

* getForEntity第二个参数String.class表示我希望返回的body类型是String

* 拿到返回结果之后，将返回结果遍历打印出来

有时候我在调用服务提供者提供的接口时，可能需要传递参数，有两种不同的方式，如下：
```
 @RequestMapping("/hello")
    public String sayHello(@RequestParam String name) {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity("http://service-hello/hello?name={1}", String.class, name);
        return responseEntity.getBody();
    }

@RequestMapping("/hello2")
    public String sayHello2(@RequestParam String name) {
        Map<String, String> map = new HashMap<>();
        map.put("name", name);
        ResponseEntity<String> responseEntity = restTemplate.getForEntity("http://service-hello/hello?name={name}", String.class, map);
        return responseEntity.getBody();
    }
```
* 可以用一个数字做占位符，最后是一个可变长度的参数，来一一替换前面的占位符

* 也可以前面使用name={name}这种形式，最后一个参数是一个map，map的key即为前边占位符的名字，map的value为参数值

第一个调用地址也可以是一个URI而不是字符串，这个时候我们构建一个URI即可，参数神马的都包含在URI中了，如下：
```
@RequestMapping("/hello3")
    public String sayHello3(@RequestParam String name) {
        UriComponents uriComponents = UriComponentsBuilder.fromUriString("http://service-hello/hello?name={name}").build().expand(name).encode();
        URI uri = uriComponents.toUri();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);
        return responseEntity.getBody();
    }
```
通过Spring中提供的UriComponents来构建Uri即可。

当然，服务提供者不仅可以返回String，也可以返回一个自定义类型的对象，比如我的服务提供者中有如下方法：
```
 @RequestMapping(value = "/getbook1", method = RequestMethod.GET)
    public Book book1() {
        return new Book("三国演义", 90, "罗贯中", "花城出版社");
    }
```
对于该方法我可以在服务消费者中通过如下方式来调用：
```
@RequestMapping("/book1")
    public Book book1() {
        ResponseEntity<Book> responseEntity = restTemplate.getForEntity("http://service-hello/getbook1", Book.class);
        return responseEntity.getBody();
    }
```
第二种：getForObject
getForObject函数实际上是对getForEntity函数的进一步封装，如果你只关注返回的消息体的内容，对其他信息都不关注，此时可以使用getForObject，举一个简单的例子，如下：
```
@RequestMapping("/book2")
    public Book book2() {
        Book book = restTemplate.getForObject("http://service-hello/getbook1", Book.class);
        return book;
    }
```

## POST请求
在RestTemplate中，POST请求可以通过如下三个方法来发起：

第一种：postForEntity
该方法和get请求中的getForEntity方法类似，如下例子：
```
@RequestMapping("/book3")
public Book book3() {
    Book book = new Book();
    book.setName("红楼梦");
    ResponseEntity<Book> responseEntity = restTemplate.postForEntity("http://service-hello/getbook2", book, Book.class);
    return responseEntity.getBody();
}
```
方法的第一参数表示要调用的服务的地址

方法的第二个参数表示上传的参数

方法的第三个参数表示返回的消息体的数据类型

我这里创建了一个Book对象，这个Book对象只有name属性有值，将之传递到服务提供者那里去，服务提供者代码如下：
```
 @RequestMapping(value = "/getbook2", method = RequestMethod.POST)
    public Book book2(@RequestBody Book book) {
        System.out.println(book.getName());
        book.setPrice(33);
        book.setAuthor("曹雪芹");
        book.setPublisher("人民文学出版社");
        return book;
    }
```
服务提供者接收到服务消费者传来的参数book，给其他属性设置上值再返回
第二种：postForObject
如果你只关注，返回的消息体，可以直接使用postForObject。用法和getForObject一致。
第三种：postForLocation
postForLocation也是提交新资源，提交成功之后，返回新资源的URI，postForLocation的参数和前面两种的参数基本一致，只不过该方法的返回值为Uri，这个只需要服务提供者返回一个Uri即可，该Uri表示新资源的位置。

## PUT请求
在RestTemplate中，PUT请求可以通过put方法调用，put方法的参数和前面介绍的postForEntity方法的参数基本一致，只是put方法没有返回值而已。
```
  @RequestMapping("/put")
    public void put() {
        Book book = new Book();
        book.setName("红楼梦");
        restTemplate.put("http://service-hello/getbook3/{1}", book, 99);
    }
```
book对象是我要提交的参数，最后的99用来替换前面的占位符{1}
## DELETE请求
delete请求我们可以通过delete方法调用来实现，如下例子：
```
@RequestMapping("/delete")
    public void delete() {
        restTemplate.delete("http://service-hello/getbook4/{1}", 100);
    }
```
delete方法也有几个重载的方法，不过重载的参数和前面基本一致，不赘述。
























