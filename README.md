# KvPref

基于属性委托的 key-value 方式存储封装

### 使用
```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
     implementation 'com.github.EspoirX:KvPref:v1.2'
}
```
[![](https://jitpack.io/v/EspoirX/KvPref.svg)](https://jitpack.io/#EspoirX/KvPref)

key-value 方式的存储相信每个项目都会用，而使用需要包装，作用是统一操作，方便使用并且如果换框架什么的时候可以轻松替换。

KvPref 是利用 kotlin 属性委托封装的一个框架，跟其他很多类似的原理差不多，网上也有很多文章，所以这里主要是看看用法合不合你心意。

## 用法
### 1. 初始化
```kotlin
KvPrefModel.initKvPref(this, object : Serializer {
    private val gson = Gson()
    override fun serializeToJson(value: Any?): String? {
        return gson.toJson(value)
    }

    override fun deserializeFromJson(json: String?, type: Type): Any? {
        return gson.fromJson(json, type);
    }
})
```
调用 initKvPref 方法初始化，传入 Application，第二个参数是序列化和反序列化的接口，因为 KvPref 是支持存储对象的，而对象存储其实是以 json 字符串形式进行。所以需要
序列化和反序列化，而具体实现通过 Serializer 接口暴露给业务，如上所示是用 Gson 实现，如果你没有存储对象的需求，则可以不传第二个参数。


### 2. 创建具体的 key-value 实现
```kotlin
interface KvPrefProvider {
    fun get(context: Context, name: String, mode: Int): SharedPreferences
}
```
key-value 的实现有很多种，比如原生的 SharedPreferences 和 mmkv 等，所以这里需要实现一个 KvPrefProvider 接口，告诉框架你的方式是什么。

比如我用 SharedPreferences：
```kotlin
class DefKvPref : KvPrefProvider {
    override fun get(context: Context, name: String, mode: Int): SharedPreferences {
        return context.getSharedPreferences(name, mode)
    }
}
```
比如我用 mmkv:
```kotlin
class MmKvPref : KvPrefProvider {
    override fun get(context: Context, name: String, mode: Int): SharedPreferences {
        if (MMKV.getRootDir().isNullOrEmpty()) {
            MMKV.initialize(context)
        }
        return MMKV.mmkvWithID(name, MMKV.SINGLE_PROCESS_MODE) as SharedPreferences
    }
}
```



### 3. 创建存放 key-value 配置的类
创建一个类，object 类型，使其继承 KvPrefModel，则完成创建。
```kotlin
object SpFileConfig : KvPrefModel("spFileName") { ... }
```
KvPrefModel 有两个参数，第一个参数是 key-value 文件的文件名，第二个参数是 KvPrefProvider，即具体实现。文件名是必需要有的，
而第二个参数可以不传，不传的话默认实现就是 SharedPreferences，如果你用的是 mmkv 你可以这样：
```kotlin
object SpFileConfig : KvPrefModel("spFileName", MmKvPref()) { ... }
```


### 4. 具体使用
```kotlin
object SpFileConfig : KvPrefModel("spFileName") {
    var people: People? by objPrefNullable(People().apply { age = 100;name = "吃狗屎" })
    var otherpeople: People by objPref()
    var name: String by stringPref()
    var otherName: String? by stringPrefNullable()
    var age: Int by intPref()
    var height: Long by longPref()
    var weight: Float by floatPref()
    var isGay: Boolean? by booleanPrefNullable(false, key = "是否是变态")
}

SpFileConfig.name = "大妈蛋"
Log.i("MainActivity", "read = " + SpFileConfig.name)
```
如上，在 SpFileConfig 里面定义了一些值，在使用的时候，给值赋值就是在写 key-value，直接获取值，就是在读 key-value。

每个值的类型对应这一个相应的 xxxPref() 方法，值的类型对应的就是读写 key-value 的具体类型，比如 stringPref
就是对应 putString 和 getString。

每个 xxxPref() 方法都有两种，一个是 xxxPref()，一个是 xxxPrefNullable，因为 kotlin 对 null 的检查是严格的，所以如果你
使用的值可能是 null 的话，请用 xxxPrefNullable 方法，其他没区别。对象对应的是 objPref() 和 objPrefNullable()

### 5. xxxPref() 方法
```kotlin
fun stringPref(
    default: String = "",
    key: String? = null,
    keyUpperCase: Boolean = isKeyUpperCase,
    synchronous: Boolean = isCommitProperties
)
```
下面看看 xxxPref() 方法可以做什么，这里用 stringPref 举例，其他方法一样。

首先每个 xxxPref() 方法都有 4 个参数，default 代表是默认值，意思就不说了，大家都知道。
key 代表是存储的 key，默认为空，在空的时候，存储是真正的 key 取的是变量名，不为空的时候取的就是这个 key。
keyUpperCase 代表是否把 key 变成大写，默认 false。
synchronous 代表是使用 apply() 还是 commit()，false 代表是使用 apply()，默认 false。

### 6. 兼容 Java 的用法

```java
SpFileConfig.INSTANCE.setName("哈哈啊")
String name = SpFileConfig.INSTANCE.getName()
```
java 可以直接这样使用。


### 7. 批量操作
如果要同时操作 N 个 key-value，就需要批量操作，因为一个个来显得不好。批量操作相关的 API 有 4 个：
```kotlin
fun beginBulkEdit()   //开始批量操作
fun applyBulkEdit()   //apply 形式结束批量操作
fun commitBulkEdit()  //commit 形式结束批量操作
fun cancelBulkEdit()  //释放资源
```

用法举例：
```kotlin
SpFileConfig.beginBulkEdit()
SpFileConfig.name = "小明"
SpFileConfig.age = 18
SpFileConfig.isGay = true
SpFileConfig.applyBulkEdit()
SpFileConfig.cancelBulkEdit()
```

可以看到代码比较模版，所以这里也提供了扩展函数去直接使用：
```kotlin
SpFileConfig.applyBulk {
    name = "小明"
    age = 18
    isGay = true
}
```
applyBulk 是调用 apply() 的，当然你也可以用 commitBulk

### 8. 动态key存储功能
在实际项目中，经常也会遇到这样的一种情况，需求存储的 key 是动态的，什么意思？

举个例子，有一个颜色配置需要跟随用户，不同的用户不一样，所以在存储的时候很可能会这样做：color_config_312312

其中 color_config_ 是一个 key 固定的部分，而后面那一串数字是不同用户的 userId。这样每个 userId 都会对应一个 key，所以 color_config 不是固定的，而是动态的。

现在看看使用 KvPref 是如何完成这种需求的。首先我们需要定义一个变量作为 key 的固定部分：
```kotlin
object SpFileConfig : KvPrefModel("spFileName") {
     var color_config: DynamicKeyPref<String> by dynamicKeyPref()
         private set
         
     var color_config: DynamicKeyPref<String?> by dynamicKeyPrefNullable()
         private set
}
```
动态key存储功能需要使用属性 DynamicKeyPref，DynamicKeyPref 是一个接口，内部有 get 和 set 方法，用来完成存取功能，DynamicKeyPref 会接受一个泛型
类型，传入的类型是什么，就代表存储对应的类型是什么。


接下来，我们看看例子：
```kotlin
 SpFileConfig.color_config.set("312312", "#FFFFFF")
 val color = SpFileConfig.color_config.get("312312")
```
如上，set 方法有两个参数，第一个是 key 的动态部分，即上面说到的 userId，第二个参数是具体的值，get 方法有一个参数，即对应的 key。  
以上例子在 sp 文件会已下面的结果出现：
```xml
 <int name="colorConfig_312312" value="#FFFFFF" />
```


在其他功能方面，跟其他情况一样，比如同样可以在批量操作中使用动态key功能：
```kotlin
 SpFileConfig.applyBulk {
    SpFileConfig.haha.set("13", "打卡时打开")
    SpFileConfig.haha1.set("24", "dasjd")
    SpFileConfig.haha2.set("13", "萨达安卡")
    name = "达拉斯多久啊离开"
}
```

### 9. 数据迁移
KvPref 提供了数据迁移方法，支持其他实现 SharedPreferences 接口的 key-value 实现把数据迁移到 KvPref。
举例：
```kotlin
class KvMigrateProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        if (context != null && context is Application) {
            KvPrefModel.initKvPref(context as Application, object : Serializer {
                private val gson = Gson()
                override fun serializeToJson(value: Any?): String? {
                    return gson.toJson(value)
                }

                override fun deserializeFromJson(json: String?, type: Type): Any? {
                    return gson.fromJson(json, type);
                }
            })
            SpFileConfig.migrate(PreferenceManager.getDefaultSharedPreferences(context))
        }
        return true
    }
    //...
```

为了尽早执行迁移逻辑，这里使用了 ContentProvider，然后顺便把初始化也放里面，通过 migrate 方法完成数据迁移。

### 10. LiveData 形式监听 SharedPreferences.OnSharedPreferenceChangeListener
如果你想监听某个字段的 OnSharedPreferenceChangeListener，可以这样做：
```kotlin
SpFileConfig.asLiveData(SpFileConfig::name).observe(this, Observer {
    //...
})
```

### 11. 其他 API
```kotlin
fun remove(property: KProperty<*>, synchronous: Boolean = isCommitProperties)
fun getPrefKey(property: KProperty<*>): String?
fun getPrefName(property: KProperty<*>): String?
fun getAll()
```

remove 就是删除的意思，getPrefKey 是获取 key-value 的 key 值，getPrefName 是获取变量名，getAll 是获取全部数据。

使用方式：
```kotlin
SpFileConfig.remove(SpFileConfig::name)
val prefKey = SpFileConfig.getPrefKey(SpFileConfig::name)
val prefName = SpFileConfig.getPrefName(SpFileConfig::name)
val map = SpFileConfig.getAll()
```
注意参数都是以双冒号的形式传进去的






