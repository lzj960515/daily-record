# SPI

SPI ，全称为 Service Provider Interface(服务提供者接口)，是一种服务发现机制。它通过在classpath路径下的META-INF/services文件夹查找文件，自动加载文件中所定义的类。

## 作用

比如在远程调用服务中，一个A服务调用B服务时，当B服务具备多个实例时，那A服务就不得不选择一种负载均衡算法进行调用，此时如果既有的负载均衡算法不能实现我们的需求，那么我们就不得不自己写一个这样的算法嵌入到原有的远程调用框架中。

那么，应当如何让实现方的实现轻松的框架到框架的调用逻辑中，就成了框架的设计者不得不考虑的问题。当然，我们知道在Spring中这些都不是事，比如框架里面有个接口叫`Rule`，然后使用者实现这个接口`MyRule`并且把这个`Bean`注入到Spring容器中，这时候选择负责均衡算法的时候判断Spring中有没有这样的Bean，有就使用它，没有就用框架里面默认的就OK～

```java
public class Executor{
  @Autowired(required = false)
  private IRule rule = new DefaultRule();
  
  public void execute(){
    rule....
  }
}
```

> 以上代码纯手写

如果没有Spring的世界呢？

在Java中，提供了一种SPI机制，通过这种机制，我们可以用指定的类加载器去实例化出某一个接口下的实现类

> 为什么是指定的类加载器呢？

具体是怎么做的呢？

## 栗子

建一个工程，结构如下

![](https://notes.zijiancode.cn/spispidemo.png)

provider 为服务提供方，可以理解为我们的框架

zoo 为使用方，因为我的服务提供接口叫`Animal`，所以所有实现都是动物～

> pom.xml里面啥都没有

### 1. 定义一个接口

在provider模块中定义接口`Animal`

```java
package cn.zijiancode.spi.provider;

/**
 * 服务提供者 动物
 */
public interface Animal {

    // 叫
    void call();
}
```

### 2. 使用该接口

在zoo模块中引入provider

```xml
<dependency>
  <groupId>cn.zijiancode</groupId>
  <artifactId>provider</artifactId>
  <version>1.0.0</version>
</dependency>
```

写一个小猫咪实现`Animal`接口

```java
public class Cat implements Animal {
    @Override
    public void call() {
        System.out.println("喵喵喵～～");
    }
}
```

写一个狗子也实现`Animal`接口

```java
public class Dog implements Animal {

    @Override
    public void call() {
        System.out.println("汪汪汪!!!");
    }
}
```

### 3. 编写配置文件

新建文件夹META-INF/services

在文件夹下新建文件`cn.zijiancode.spi.provider.Animal`

> 对，你没看错，接口的全限定类名就是文件名

编辑文件

```
cn.zijiancode.spi.zoo.Dog
cn.zijiancode.spi.zoo.Cat
```

> 里面放实现类
>
> 至于为啥这样做，别问，问就是规定

### 3. 测试

```java
package cn.zijiancode.spi.zoo.test;

import cn.zijiancode.spi.provider.Animal;

import java.util.ServiceLoader;

public class SpiTest {

    public static void main(String[] args) {
        // 使用Java的ServiceLoader进行加载
        ServiceLoader<Animal> load = ServiceLoader.load(Animal.class);
        load.forEach(Animal::call);
    }
}
```

测试结果：

```
汪汪汪!!!
喵喵喵～～
```

整个项目结构如下：

![](https://notes.zijiancode.cn/spispifull.png)

### 原理分析

其实这里面涉及的原理不难猜想：SeviceLoader根据规则读取出文件中的内容，然后逐一就行实例化

## 小结

看完本篇内容，相信大家已经明白了什么是SPI已经如何使用它，简单回顾一下

- SPI: 服务提供者接口，是一种服务发现机制
- 意义：框架或者服务提供者本身可以不实现内容自定义规范，由使用者进行实现：如 java.sql.Driver

问题：如果细心一点的话可以发现，这个SPI机制是可以允许多个实现的，如我们的狗子和猫咪，那么，如果我们需要在众多的实现中选择一个实现，应当如果做呢？

原生的JavaSPI机制是做不到的，但是Dubbo给我们实现了一个这样的类加载器`ExtensionLoader`

更多的内容就由小伙伴自行探索啦～