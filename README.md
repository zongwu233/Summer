# Summer

## 介绍
Summer是一个模块解耦框架，它解决了以下几个痛点：

1、模块与模块之间不再需要相互依赖

2、模块边界清晰化

<!--
业务模块间通常通过定义/实现java的interface完成业务逻辑，必然导致模块间存在代码层面的依赖。也导致编译期的工程依赖。事实上，业务模块间仅仅是逻辑上存在依赖，完全没必要产生实际的工程依赖。			
该组件提供了一种解藕模块间显式依赖的能力。				
同时还提供了一个副作用：只要方法签名一致，就可以视为实现了该接口。（这是某些编程语言实现接口的方式）	
-->		
##	使用
###	模块A

-	在build.gradle加入

```java
	compile 'com.meiyou:summer:1.0.4'
	compile 'com.meiyou:summer-compiler:1.0.4'

```

-	定义接口：

				
```java	
@ProtocolShadow("ModuleBarStub")
public interface ModuleStub {
    public void testMethod(String msg, Context context, TextView textView);
    }

```
其中：

ProtocolShadow 是summer接口标识，意味着在编译时会被扫描到并做相应的处理

ModuleBarStub 则是协商的名字，模块B将使用这个字符串标识与之联系

###	模块B					

-	在build.gradle加入

```java
	compile 'com.meiyou:summer:1.0.4'
	compile 'com.meiyou:summer-compiler:1.0.4'

```

-	定义实现：

```java
@Protocol("ModuleBarStub")
public class ModuleBar {
    
    public void testMethod(String msg, Context context,
                                        TextView textView) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        
    }
}
```
其中：

Protocol 是summer实现标识，意味着在编译时会被扫描到并做相应的处理

ModuleBarStub 则是与模块A协商的名字

-	模块B调用模块A的方法：

```java
 ProtocolInterpreter.getDefault().
                        create(ModuleStub.class)
                        .testMethod("oh this from main Activity!",
                                getApplicationContext(), textView);
                               
```


至此，模块A和模块B，在不相互依赖的情况下，完成了交互。

				

## 实现原理

通过编译期注解＋java动态代理实现。				
具体细节见代码。	

##### 缺点

之前可以通过implements interface 比较方便地获得子类方法的签名，现在没有IDE智能提示，写实际的实现类方法的时候，有点不方便。			

##### 混淆
-keep public class com.meiyou.framework.summer.data.** { *; }

##### 提供模块间数据总线功能

提供类似于EventBus 的功能，但是属于模块间可用（也支持模块内自己通信）。用法类似于EventBus。除了要注意：

发送消息的模块，要对Event 的class做注解@Event("name")

在响应消息的模块的具体方法上，对方法注解@OnEvent("name")即可，方法名称不受限制，但是改方法的入参，必须与原始Event的class同构。

**同构：具有相同的结构组成；相同的成员变量。**

OnEvent注解支持MainThread和BackgroundThread。         
示例:         

事件源Event：

```java
@Event("Account")
public class AccountDO {
    String nick = "ooooh";
    long userId = 2222;

    public AccountDO(String nick, long userId) {
        this.nick = nick;
        this.userId = userId;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
```

接收改事件的对象需要注册,比如：

```java
 ProtocolInterpreter.getDefault().register(testEvent);
```

多次调用该方法会自动去重，也可以调用下面方法检测是否已经注册：

```java
boolean registered =  ProtocolInterpreter.getDefault().isRegister(testEvent);
```

发送该事件(可以在任何活动线程执行)：

```java
  ProtocolInterpreter.getDefault().post(new AccountDO("bg", 2222));
```

testEvent 的class里面有：

```java
	@OnEvent("Account")
    public void process(MyAccount account) {
        ToastUtils.showToast(mContext,"module test get: "+account.getNick() + "," + account.getUserId());
    }

    @OnEvent(value = "Account",exec = OnEvent.Thread.BACK_GROUND)
    public void process2(MyAccount account) {
        LogUtils.d(TAG,"module test background get: "+account.getNick() + "," + account.getUserId());
    }
```

`process()` `process2()` 两个方法注册接收改事件，分别是在MainThread BackgroundThread里执行。

**如果不指定**`OnEvent`里的`exec`**就意味着默认在mainThread执行。**




## [License Apache-2.0](LICENSE)
​		





