# JCE加密Cipher类说明(详细)

javax.crypto.Cipher类提供加密和解密功能，该类是JCE框架的核心。

## 一，与所有的引擎类一样，可以通过调用Cipher类中的getInstance静态工厂方法得到Cipher对象。

public static Cipher getInstance(String transformation);

public static Cipher getInstance(String transformation,String provider);

参数transformation是一个字符串，它描述了由指定输入产生输出所进行的操作或操作集合。

参数transformation总是包含密码学算法名称，比如DES，也可以在后面包含模式和填充方式。

参数transformation可以是下列两种形式之一：

“algorithm/mode/padding”

“algorithm”

例如下面的例子就是有效的transformation形式：

"DES/CBC/PKCS5Padding"

"DES"

如 果没有指定模式或填充方式，就使用特定提供者指定的默认模式或默认填充方式。例如，SunJCE提供者使用ECB作为DES、DES-EDE和 Blowfish等Cipher的默认模式，并使用PKCS5Padding作为它们默认的填充方案。这意味着在SunJCE提供者中，下列形式的声明是 等价的：Cipher c1=Cipher.getInstance("DES/ECB/PKCS5Padding");

     Cipher c1=Cipher.getInstance("DES");

当 以流加密方式请求以块划分的cipher时，可以在模式名后面跟上一次运算需要操作的bit数目，例如采用"DES/CFB8/NoPadding"和 "DES/OFB32/PKCS5Padding"形式的transformation参数。如果没有指定数目，则使用提供者指定的默认值（例如 SunJCE提供者使用的默认值是64bit）。

getInstance工厂方法返回的对象没有进行初始化，因此在使用前必须进行初始化。

通过getInstance得到的Cipher对象必须使用下列四个模式之一进行初始化，这四个模式在Cipher类中被定义为final integer常数，我们可以使用符号名来引用这些模式：

ENCRYPT_MODE,加密数据

DECRYPT_MODE,解密数据

WRAP_MODE,将一个Key封装成字节，可以用来进行安全传输

UNWRAP_MODE,将前述已封装的密钥解开成java.security.Key对象

每个Cipher初始化方法使用一个模式参数opmod，并用此模式初始化Cipher对象。此外还有其他参数，包括密钥key、包含密钥的证书certificate、算法参数params和随机源random。

我们可以调用以下的init方法之一来初始化Cipher对象：

public void init(int opmod,Key key);

public void init(int opmod,Certificate certificate);

public void init(int opmod,Key key,SecureRandom random);

public void init(int opmod,Certificate certificate,SecureRandom random);

public void init(int opmod,Key key,AlgorithmParameterSpec params);

public void init(int opmod,Key key,AlgorithmParameterSpec params,SecureRandom random);

public void init(int opmod,Key key,AlgorithmParameters params);

public void init(int opmod,Key key,AlgorithmParameters params,SecureRandom random);

必须指出的是，加密和解密必须使用相同的参数。当Cipher对象被初始化时，它将失去以前得到的所有状态。即，初始化Cipher对象与新建一个Cipher实例然后将它初始化是等价的。

## 二，可以调用以下的doFinal（）方法之一完成单步的加密或解密数据：

public byte[] doFinal(byte[] input);

public byte[] doFinal(byte[] input,int inputOffset,int inputLen);

public int doFinal(byte[] input,int inputOffset,int inputLen,byte[] output);

public int doFinal(byte[] input,int inputOffset,int inputLen,byte[] output,int outputOffset);

在多步加密或解密数据时，首先需要一次或多次调用update方法，用以提供加密或解密的所有数据：

public byte[] update(byte[] input);

public byte[] update(byte[] input,int inputOffset,int inputLen);

public int update(byte[] input,int inputOffset,int inputLen,byte[] output);

public int update(byte[] input,int inputOffset,int inputLen,byte[] output,int outputOffset);

如果还有输入数据，多步操作可以使用前面提到的doFinal方法之一结束。如果没有数据，多步操作可以使用下面的doFinal方法之一结束：

public byte[] doFinal();

public int doFinal(byte[] output,int outputOffset);

如果在transformation参数部分指定了padding或unpadding方式，则所有的doFinal方法都要注意所用的padding或unpadding方式。

调用doFinal方法将会重置Cipher对象到使用init进行初始化时的状态，就是说，Cipher对象被重置，使得可以进行更多数据的加密或解密，至于这两种模式，可以在调用init时进行指定。

## 三，包裹wrap密钥必须先使用WRAP_MODE初始化Cipher对象，然后调用以下方法：

public final byte[] wrap(Key key);

如果将调用wrap方法的结果（wrap后的密钥字节）提供给解包裹unwrap的人使用，必须给接收者发送以下额外信息：

（1）密钥算法名称:

      密钥算法名称可以调用Key接口提供的getAlgorithm方法得到：

      public String getAlgorithm();

（2）被包裹密钥的类型（Cipher.SECRET_KEY,Cipher.PRIVATE_KEY,Cipher.PUBLIC_KEY)

sourcelink: http://bbs.sdu.edu.cn/pc/pccon.php?id=1292&nid=41716&order=&tid=


为了对调用wrap方法返回的字节进行解包，必须先使用UNWRAP_MODE模式初始化Cipher对象，然后调用以下方法 ：

public final Key unwrap(byte[] wrappedKey,String wrappedKeyAlgorithm,int wrappedKeyType));

其 中，参数wrappedKey是调用wrap方法返回的字节，参数wrappedKeyAlgorithm是用来包裹密钥的算法，参数 wrappedKeyType是被包裹密钥的类型，该类型必须是Cipher.SECRET_KEY,Cipher.PRIVATE_KEY, Cipher.PUBLIC_KEY三者之一。

## 四，SunJCE提供者实现的cipher算法使用如下参数：

（1）采用CBC、CFB、OFB、PCBC模式的DES、DES-EDE和Blowfish算法。，它们使用初始化向量IV作为参数。可以使用javax.crypto.spec.IvParameterSpec类并使用给定的IV参数来初始化Cipher对象。

（2）PBEWithMD5AndDES使用的参数是一个由盐值和迭代次数组成的参数集合。可以使用javax.crypto.spec.PBEParameterSpec类并利用给定盐值和迭代次数来初始化Cipher对象。

注意：如果使用SealedObject类，就不必为解密运算参数的传递和保存担心。这个类在加密对象内容中附带了密封和加密的参数，可以使用相同的参数对其进行解封和解密。

Cipher 中的某些update和doFinal方法允许调用者指定加密或解密数据的输出缓存。此时，保证指定的缓存足够大以容纳加密或解密运算的结果是非常重要 的，可以使用Cipher的以下方法来决定输出缓存应该有多大：public int getOutputSize(int inputLen)