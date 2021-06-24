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
     implementation 'com.github.EspoirX:KvPref:v1.1'
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
object SpFileDemo : KvPrefModel("spFileName") { ... }
```
KvPrefModel 有两个参数，第一个参数是 key-value 文件的文件名，第二个参数是 KvPrefProvider，即具体实现。文件名是必需要有的，
而第二个参数可以不传，不传的话默认实现就是 SharedPreferences，如果你用的是 mmkv 你可以这样：
```kotlin
object SpFileDemo : KvPrefModel("spFileName", MmKvPref()) { ... }
```


### 4. 具体使用
```kotlin
object SpFileDemo : KvPrefModel("spFileName") {
    var people: People? by objPrefNullable(People().apply { age = 100;name = "吃狗屎" })
    var otherpeople: People by objPref()
    var name: String by stringPref()
    var otherName: String? by stringPrefNullable()
    var age: Int by intPref()
    var height: Long by longPref()
    var weight: Float by floatPref()
    var isGay: Boolean? by booleanPrefNullable(false, key = "是否是变态")
}

SpFileDemo.name = "大妈蛋"
Log.i("MainActivity", "read = " + SpFileDemo.name)
```
如上，在 SpFileDemo 里面定义了一些值，在使用的时候，给值赋值就是在写 key-value，直接获取值，就是在读 key-value。

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
```kotlin
object SpFileDemoJava {
    fun setPeople(people: People) {
        SpFileDemo.people = people
    }

    fun getPeople() = SpFileDemo.people
}
```
因为属性委托不能直接用在 Java 代码上，所以只能麻烦一点再包装一层，也还好把...


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
SpFileDemo.beginBulkEdit()
SpFileDemo.name = "小明"
SpFileDemo.age = 18
SpFileDemo.isGay = true
SpFileDemo.applyBulkEdit()
SpFileDemo.cancelBulkEdit()
```

可以看到代码比较模版，所以这里也提供了扩展函数去直接使用：
```kotlin
SpFileDemo.applyBulk {
    name = "小明"
    age = 18
    isGay = true
}
```

### 8. 数据迁移
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
            SpFileDemo.migrate(PreferenceManager.getDefaultSharedPreferences(context))
        }
        return true
    }
    //...
```

为了尽早执行迁移逻辑，这里使用了 ContentProvider，然后顺便把初始化也放里面，通过 migrate 方法完成数据迁移。

### 9. LiveData 形式监听 SharedPreferences.OnSharedPreferenceChangeListener
如果你想监听某个字段的 OnSharedPreferenceChangeListener，可以这样做：
```kotlin
SpFileDemo.asLiveData(SpFileDemo::name).observe(this, Observer {
    //...
})
```

### 10. 其他 API
```kotlin
fun remove(property: KProperty<*>, synchronous: Boolean = isCommitProperties)
fun getPrefKey(property: KProperty<*>): String?
fun getPrefName(property: KProperty<*>): String?
fun getAll()
```

remove 就是删除的意思，getPrefKey 是获取 key-value 的 key 值，getPrefName 是获取变量名，getAll 是获取全部数据。

使用方式：
```kotlin
SpFileDemo.remove(SpFileDemo::name)
val prefKey = SpFileDemo.getPrefKey(SpFileDemo::name)
val prefName = SpFileDemo.getPrefName(SpFileDemo::name)
val map = SpFileDemo.getAll()
```
注意参数都是以双冒号的形式传进去的






