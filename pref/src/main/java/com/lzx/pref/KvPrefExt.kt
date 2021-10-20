package com.lzx.pref

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.lzx.pref.property.*
import java.lang.reflect.Type
import java.util.Locale
import kotlin.reflect.KClass
import kotlin.reflect.KProperty0

fun KvPrefModel.stringPref(
    default: String = "",
    key: String? = null,
    keyUpperCase: Boolean = KvPrefModel.isKeyUpperCase,
    synchronous: Boolean = KvPrefModel.isCommitProperties
) = KvPrefProperty(String::class, String::class.java, synchronous, key, keyUpperCase, default)

fun KvPrefModel.stringPrefNullable(
    default: String = "",
    key: String? = null,
    keyUpperCase: Boolean = KvPrefModel.isKeyUpperCase,
    synchronous: Boolean = KvPrefModel.isCommitProperties
) = KvPrefNullableProperty(String::class, String::class.java, synchronous, key, keyUpperCase, default)

fun KvPrefModel.intPref(
    default: Int = 0,
    key: String? = null,
    keyUpperCase: Boolean = KvPrefModel.isKeyUpperCase,
    synchronous: Boolean = KvPrefModel.isCommitProperties
) = KvPrefProperty(Int::class, Int::class.java, synchronous, key, keyUpperCase, default)

fun KvPrefModel.intPrefNullable(
    default: Int = 0,
    key: String? = null,
    keyUpperCase: Boolean = KvPrefModel.isKeyUpperCase,
    synchronous: Boolean = KvPrefModel.isCommitProperties
) = KvPrefNullableProperty(Int::class, Int::class.java, synchronous, key, keyUpperCase, default)

fun KvPrefModel.longPref(
    default: Long = 0,
    key: String? = null,
    keyUpperCase: Boolean = KvPrefModel.isKeyUpperCase,
    synchronous: Boolean = KvPrefModel.isCommitProperties
) = KvPrefProperty(Long::class, Long::class.java, synchronous, key, keyUpperCase, default)

fun KvPrefModel.longPrefNullable(
    default: Long = 0,
    key: String? = null,
    keyUpperCase: Boolean = KvPrefModel.isKeyUpperCase,
    synchronous: Boolean = KvPrefModel.isCommitProperties
) = KvPrefNullableProperty(Long::class, Long::class.java, synchronous, key, keyUpperCase, default)

fun KvPrefModel.floatPref(
    default: Float = 0f,
    key: String? = null,
    keyUpperCase: Boolean = KvPrefModel.isKeyUpperCase,
    synchronous: Boolean = KvPrefModel.isCommitProperties
) = KvPrefProperty(Float::class, Float::class.java, synchronous, key, keyUpperCase, default)

fun KvPrefModel.floatPrefNullable(
    default: Float = 0f,
    key: String? = null,
    keyUpperCase: Boolean = KvPrefModel.isKeyUpperCase,
    synchronous: Boolean = KvPrefModel.isCommitProperties
) = KvPrefNullableProperty(Float::class, Float::class.java, synchronous, key, keyUpperCase, default)

fun KvPrefModel.booleanPref(
    default: Boolean = false,
    key: String? = null,
    keyUpperCase: Boolean = KvPrefModel.isKeyUpperCase,
    synchronous: Boolean = KvPrefModel.isCommitProperties
) = KvPrefProperty(Boolean::class, Boolean::class.java, synchronous, key, keyUpperCase, default)

fun KvPrefModel.booleanPrefNullable(
    default: Boolean = false,
    key: String? = null,
    keyUpperCase: Boolean = KvPrefModel.isKeyUpperCase,
    synchronous: Boolean = KvPrefModel.isCommitProperties
) = KvPrefNullableProperty(Boolean::class, Boolean::class.java, synchronous, key, keyUpperCase, default)

inline fun <reified T : Any> KvPrefModel.objPref(
    default: T? = null,
    key: String? = null,
    keyUpperCase: Boolean = KvPrefModel.isKeyUpperCase,
    synchronous: Boolean = KvPrefModel.isCommitProperties
) = KvPrefProperty(T::class, object : TypeToken<T>() {}.type, synchronous, key, keyUpperCase, default)

inline fun <reified T : Any> KvPrefModel.objPrefNullable(
    default: T?,
    key: String? = null,
    keyUpperCase: Boolean = KvPrefModel.isKeyUpperCase,
    synchronous: Boolean = KvPrefModel.isCommitProperties
) = KvPrefNullableProperty(T::class, object : TypeToken<T>() {}.type, synchronous, key, keyUpperCase, default)

inline fun <reified T : Any> KvPrefModel.dynamicKeyPref(
    default: T? = null,
    key: String? = null,
    keyUpperCase: Boolean = KvPrefModel.isKeyUpperCase,
    synchronous: Boolean = KvPrefModel.isCommitProperties
) = KvDynamicPrefProperty(this, T::class, synchronous, key, keyUpperCase, default)

inline fun <reified T : Any> KvPrefModel.dynamicKeyPrefNullable(
    default: T? = null,
    key: String? = null,
    keyUpperCase: Boolean = KvPrefModel.isKeyUpperCase,
    synchronous: Boolean = KvPrefModel.isCommitProperties
) = KvDynamicNullableProperty(this, T::class, synchronous, key, keyUpperCase, default)


/**
 * 批量写入
 */
inline fun <T : KvPrefModel> T.applyBulk(block: T.() -> Unit) {
    beginBulkEdit()
    runCatching {
        block()
    }.onSuccess {
        applyBulkEdit()
        cancelBulkEdit()
    }.onFailure {
        it.printStackTrace()
        cancelBulkEdit()
    }
}

/**
 * 批量写入
 */
inline fun <T : KvPrefModel> T.commitBulk(block: T.() -> Unit) {
    beginBulkEdit()
    runCatching {
        block()
    }.onSuccess {
        commitBulkEdit()
        cancelBulkEdit()
    }.onFailure {
        it.printStackTrace()
        cancelBulkEdit()
    }
}

/**
 * commit or apply
 */
fun SharedPreferences.Editor.execute(synchronous: Boolean) {
    if (synchronous) commit() else apply()
}

/**
 * 获取 value
 */
@Suppress("UNCHECKED_CAST")
internal fun <T : Any> SharedPreferences.getPreference(
    clazz: KClass<T>, type: Type, key: String?, default: T?
): T? {
    val isDefNull = default == null
    return when (clazz) {
        Int::class -> {
            val def = if (isDefNull) 0 else default as Int
            getInt(key, def) as T?
        }
        Float::class -> {
            val def = if (isDefNull) 0f else default as Float
            getFloat(key, def) as T?
        }
        Long::class -> {
            val def = if (isDefNull) 0 else default as Long
            getLong(key, def) as T?
        }
        Boolean::class -> {
            val def = if (isDefNull) false else default as Boolean
            getBoolean(key, def) as T?
        }
        String::class -> {
            val def = if (isDefNull) "" else default as String?
            getString(key, def) as T?
        }
        else -> {
            runCatching {
                val json = getString(key, null)
                json?.deserialize(type) ?: default
            }.getOrElse { default }
        }
    }
}

/**
 * 存储 value
 */
internal fun SharedPreferences.Editor.setPreference(
    clazz: KClass<*>, key: String, value: Any
): SharedPreferences.Editor {
    when (clazz) {
        Int::class -> putInt(key, value as Int)
        Float::class -> putFloat(key, value as Float)
        Long::class -> putLong(key, value as Long)
        Boolean::class -> putBoolean(key, value as Boolean)
        String::class -> putString(key, value as String)
        else -> {
            runCatching {
                val message = value.serialize()
                putString(key, message)
            }.onFailure { it.printStackTrace() }
        }
    }
    return this
}

/**
 * 存储 value，批量用
 */
internal fun SharedPreferences.Editor.setEditor(
    clazz: KClass<*>, key: String, value: Any
): SharedPreferences.Editor {
    when (clazz) {
        Int::class -> putInt(key, value as Int)
        Float::class -> putFloat(key, value as Float)
        Long::class -> putLong(key, value as Long)
        Boolean::class -> putBoolean(key, value as Boolean)
        String::class -> putString(key, value as String)
        else -> {
            runCatching {
                val message = value.serialize()
                putString(key, message)
            }.onFailure { it.printStackTrace() }
        }
    }
    return this
}

/**
 * 序列化
 */
private fun <T> T.serialize() = KvPrefModel.serializer?.serializeToJson(this)

/**
 * 反列化
 */
@Suppress("UNCHECKED_CAST")
private fun <T : Any> String.deserialize(type: Type): T? =
    KvPrefModel.serializer?.deserializeFromJson(this, type) as? T

/**
 * LiveData，监听 OnSharedPreferenceChangeListener
 */
fun <T> KvPrefModel.asLiveData(property: KProperty0<T>): LiveData<T> {
    return object : LiveData<T>(), SharedPreferences.OnSharedPreferenceChangeListener {

        private val key: String =
            this@asLiveData.getPrefKey(property) ?: throw IllegalArgumentException(
                "Failed to get preference key," +
                        " check property ${property.name} is delegated to KvPrefModel"
            )

        override fun onSharedPreferenceChanged(prefs: SharedPreferences?, propertyName: String?) {
            runCatching {
                if (propertyName == key) {
                    postValue(property.get())
                }
            }.onFailure { it.printStackTrace() }
        }

        override fun onActive() {
            super.onActive()
            runCatching {
                this@asLiveData.preference.registerOnSharedPreferenceChangeListener(this)
                if (value != property.get()) {
                    value = property.get()
                }
            }.onFailure { it.printStackTrace() }
        }

        override fun onInactive() {
            super.onInactive()
            runCatching {
                this@asLiveData.preference.unregisterOnSharedPreferenceChangeListener(this)
            }.onFailure { it.printStackTrace() }
        }
    }
}

/**
 * 转大写
 */
fun String.upperKey(keyUpperCase: Boolean): String {
    return if (keyUpperCase) this.toUpperCase(Locale.getDefault()) else this
}

