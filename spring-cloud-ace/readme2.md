# Java 环境下使用 AES 加密的特殊问题处理
在 Java 环境下使用 AES 加密，在密钥长度和字节填充方面有一些比较特殊的处理。

1. 密钥长度问题
    默认 Java 中仅支持 128 位密钥，当使用 256 位密钥的时候，会报告密钥长度错误

    Invalid AES key length
   你需要下载一个支持更长密钥的包。这个包叫做 Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files 6，可以从这里下载，下载地址：http://www.oracle.com/technetwork/java/javase/downloads/jce-6-download-429243.html

   下载之后，解压后，可以看到其中包含两个包：

    local_policy.jar

    US_export_policy.jar

    看一下你的 JRE 环境，将 JRE 环境中 lib\lib\security 中的同名包替换掉。

2. Base64 问题
     Apache 提供了 Base64 的实现，可以从这里下载。

    下载地址：http://commons.apache.org/proper/commons-codec/download_codec.cgi

     编码
````
// 编码
String asB64 = new Base64().encodeToString("some string".getBytes("utf-8"));
System.out.println(asB64); // 输出为: c29tZSBzdHJpbmc=
````
     解码   
````
// 解码
byte[] asBytes = new Base64().getDecoder().decode("c29tZSBzdHJpbmc=");
System.out.println(new String(asBytes, "utf-8")); // 输出为: some string
```` 

     如果你已经使用 Java 8，那么就不需要再选用第三方的实现了，在 java.util 包中已经包含了 Base64 的处理。

     编码的方式
````
// 编码
String asB64 = Base64.getEncoder().encodeToString("some string".getBytes("utf-8"));
System.out.println(asB64); // 输出为: c29tZSBzdHJpbmc=
````
     解码处理
````
// 解码
byte[] asBytes = Base64.getDecoder().decode("c29tZSBzdHJpbmc=");
System.out.println(new String(asBytes, "utf-8")); // 输出为: some string
```` 

3. 关于 PKCS5 和 PKCS7 填充问题
PKCS #7 填充字符串由一个字节序列组成，每个字节填充该填充字节序列的长度。

假定块长度为 8，数据长度为 9，
          数据： FF FF FF FF FF FF FF FF FF
PKCS7 填充： FF FF FF FF FF FF FF FF FF 07 07 07 07 07 07 07

简单地说, PKCS5, PKCS7和SSL3, 以及CMS(Cryptographic Message Syntax)

有如下相同的特点:
1)填充的字节都是一个相同的字节
2)该字节的值,就是要填充的字节的个数

如果要填充8个字节,那么填充的字节的值就是0×8;
要填充7个字节,那么填入的值就是0×7;

…

如果只填充１个字节，那么填入的值就是0×1;

这种填充方法也叫PKCS5, 恰好8个字节时还要补8个字节的0×08

正是这种即使恰好是8个字节也需要再补充字节的规定，可以让解密的数据很确定无误的移除多余的字节。
![填充说明][1]


 

在PKCS# Padding中说:

因为恢复的明文的最后一个字节 告诉你 存在多少个填充字节, 用PKCS#5 填充 的加密方法, 即使在输入的明文长度 恰好是 块大小(Block Size)整数倍 , 也会增加一个完整的填充块. 否则,恢复出来的明文的最后一个字节可能是实际的消息字节.
因为第1个因素限制了 使用PKCS#填充的 对称加密算法的 输入块大小(Block Size, 注意不是输入的明文的总长度 total input length), 最大只能是256个字节.   因为大多数对称块加密算法 通常使用8字节或者16字节的块, 所以,这不是一个问题
使用ECB模式填充可能会有安全问题.
使用PKCS#5填充 可以很方便地检测明文中的错误.
标准

PKCS #7: Cryptographic Message Syntax

在 10.3节中讲到了上面提到的填充算法,  对Block Size并没有做规定

PKCS #5: Password-Based Cryptography Specification

在6.1.1 中对 填充做了说明
但是因为该标准 只讨论了 8字节(64位) 块的加密, 对其他块大小没有做说明
其 填充算法跟 PKCS7是一样的

后来 AES 等算法, 把BlockSize扩充到 16个字节

比如, Java中
`Cipher.getInstance(“AES/CBC/PKCS5Padding”)`
这个加密模式
跟C#中的
````
RijndaelManaged cipher = new RijndaelManaged();
cipher.KeySize = 128;
cipher.BlockSize = 128;
cipher.Mode = CipherMode.CBC;
cipher.Padding = PaddingMode.PKCS7;
````
的加密模式是一样的

因为AES并没有64位的块, 如果采用PKCS5, 那么实质上就是采用PKCS7

  [1]: http://zhiwei.li/text/wp-content/uploads/2009/05/pkcs7_padding.jpg