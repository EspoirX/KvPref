package com.lzx.pref

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.SystemClock
import com.lzx.pref.property.KvDynamicPrefProperty
import com.lzx.pref.property.KvPrefNullableProperty
import com.lzx.pref.property.KvPrefProperty
import java.lang.reflect.Type
import kotlin.reflect.KProperty

open class KvPrefModel constructor(
    private val fileName: String,
    private val provider: KvPrefProvider = DefKvPref()
) {
    companion object {
        lateinit var context: Application
        internal var serializer: Serializer? = null
        var isCommitProperties = false  //commit或者apply，默认apply
        var isKeyUpperCase = false //key是否大写

        /**
         * 初始化
         */
        fun initKvPref(context: Application, serializer: Serializer? = null) {
            this.context = context
            this.serializer = serializer
        }
    }

    //是否批量存储
    internal var isInTransaction: Boolean = false
    internal var transactionStartTime: Long = Long.MAX_VALUE

    //preference
    internal val preference: SharedPreferences by lazy {
        provider.get(context, fileName, Context.MODE_PRIVATE)
    }

    //批量存储的时候用到
    internal var editor: SharedPreferences.Editor? = null

    //保存所有 key和value
    internal val kvProperties = mutableMapOf<String, PreferenceProperty>()

    fun stringPref(
        default: String = "",
        key: String? = null,
        keyUpperCase: Boolean = isKeyUpperCase,
        synchronous: Boolean = isCommitProperties
    ) = StringPref(default, synchronous, key, keyUpperCase)

    fun stringPrefNullable(
        default: String = "",
        key: String? = null,
        keyUpperCase: Boolean = isKeyUpperCase,
        synchronous: Boolean = isCommitProperties
    ) = StringNullablePref(default, synchronous, key, keyUpperCase)

    fun intPref(
        default: Int = 0,
        key: String? = null,
        keyUpperCase: Boolean = isKeyUpperCase,
        synchronous: Boolean = isCommitProperties
    ) = IntPref(default, synchronous, key, keyUpperCase)

    fun intPrefNullable(
        default: Int = 0,
        key: String? = null,
        keyUpperCase: Boolean = isKeyUpperCase,
        synchronous: Boolean = isCommitProperties
    ) = IntNullablePref(default, synchronous, key, keyUpperCase)

    fun longPref(
        default: Long = 0,
        key: String? = null,
        keyUpperCase: Boolean = isKeyUpperCase,
        synchronous: Boolean = isCommitProperties
    ) = LongPref(default, synchronous, key, keyUpperCase)

    fun longPrefNullable(
        default: Long = 0,
        key: String? = null,
        keyUpperCase: Boolean = isKeyUpperCase,
        synchronous: Boolean = isCommitProperties
    ) = LongNullablePref(default, synchronous, key, keyUpperCase)

    fun floatPref(
        default: Float = 0f,
        key: String? = null,
        keyUpperCase: Boolean = isKeyUpperCase,
        synchronous: Boolean = isCommitProperties
    ) = FloatPref(default, synchronous, key, keyUpperCase)

    fun floatPrefNullable(
        default: Float = 0f,
        key: String? = null,
        keyUpperCase: Boolean = isKeyUpperCase,
        synchronous: Boolean = isCommitProperties
    ) = FloatNullablePref(default, synchronous, key, keyUpperCase)

    fun booleanPref(
        default: Boolean = false,
        key: String? = null,
        keyUpperCase: Boolean = isKeyUpperCase,
        synchronous: Boolean = isCommitProperties
    ) = BooleanPref(default, synchronous, key, keyUpperCase)

    fun booleanPrefNullable(
        default: Boolean = false,
        key: String? = null,
        keyUpperCase: Boolean = isKeyUpperCase,
        synchronous: Boolean = isCommitProperties
    ) = BooleanNullablePref(default, synchronous, key, keyUpperCase)

    inline fun <reified T : Any> objPref(
        default: T? = null,
        key: String? = null,
        keyUpperCase: Boolean = isKeyUpperCase,
        synchronous: Boolean = isCommitProperties
    ) = KvPrefProperty(
        T::class,
        object : TypeToken<T>() {}.type,
        synchronous,
        key,
        keyUpperCase,
        default
    )

    inline fun <reified T : Any> objPrefNullable(
        default: T,
        key: String? = null,
        keyUpperCase: Boolean = isKeyUpperCase,
        synchronous: Boolean = isCommitProperties
    ) = KvPrefNullableProperty(
        T::class,
        object : TypeToken<T>() {}.type,
        synchronous,
        key,
        keyUpperCase,
        default
    )

    inline fun <reified T : Any> dynamicKeyPref(
        default: T? = null,
        key: String? = null,
        keyUpperCase: Boolean = isKeyUpperCase,
        synchronous: Boolean = isCommitProperties
    ) = KvDynamicPrefProperty(
        this,
        T::class,
        synchronous,
        key,
        keyUpperCase,
        default
    )


    /**
     * 开始批量存储
     */
    @SuppressLint("CommitPrefEdits")
    fun beginBulkEdit() {
        isInTransaction = true
        transactionStartTime = SystemClock.uptimeMillis()
        editor = preference.edit()
    }

    /**
     * 结束批量存储，apply
     */
    fun applyBulkEdit() {
        editor?.apply()
        isInTransaction = false
    }

    /**
     * 结束批量存储，commit
     */
    fun commitBulkEdit() {
        editor?.commit()
        isInTransaction = false
    }

    /**
     * 取消批量存储
     */
    fun cancelBulkEdit() {
        editor = null
        isInTransaction = false
    }

    /**
     * 获取 key 值
     */
    fun getPrefKey(property: KProperty<*>, applyKey: String? = null): String? {
        val key = if (applyKey.isNullOrEmpty()) property.name else property.name + "_" + applyKey
        return kvProperties[key]?.preferenceKey()
    }

    /**
     * 获取key变量名
     */
    fun getPrefName(property: KProperty<*>, applyKey: String? = null): String? {
        val key = if (applyKey.isNullOrEmpty()) property.name else property.name + "_" + applyKey
        return kvProperties[key]?.propertyName
    }

    /**
     * remove
     */
    @SuppressLint("CommitPrefEdits")
    fun remove(
        property: KProperty<*>,
        applyKey: String? = null,
        synchronous: Boolean = isCommitProperties
    ) {
        preference.edit().remove(getPrefKey(property, applyKey)).execute(synchronous)
    }

    /**
     * getAll
     */
    fun getAll() = preference.all

    /**
     * 迁移
     */
    fun migrate(migratePreference: SharedPreferences) {
        val spFinishMigrate = preference.getBoolean("spFinishMigrate", false)
        if (spFinishMigrate) return

        beginBulkEdit()
        runCatching {
            editor?.setEditor(Boolean::class, "spFinishMigrate", false)
            migratePreference.all.forEach {
                it.value?.let { value -> editor?.setEditor(value::class, it.key, value) }
            }
        }.onSuccess {
            applyBulkEdit()
            cancelBulkEdit()
            editor?.setEditor(Boolean::class, "spFinishMigrate", true)
        }.onFailure {
            it.printStackTrace()
            cancelBulkEdit()
        }
    }
}

/**
 * 序列化和反序列化接口
 */
interface Serializer {

    fun serializeToJson(value: Any?): String?

    fun deserializeFromJson(json: String?, type: Type): Any?
}

