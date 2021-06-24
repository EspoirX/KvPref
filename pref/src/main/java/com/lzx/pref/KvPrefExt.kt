package com.lzx.pref

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.KProperty0

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
): T? = when (clazz) {
    Int::class -> getInt(key, default as Int) as T?
    Float::class -> getFloat(key, default as Float) as T?
    Long::class -> getLong(key, default as Long) as T?
    Boolean::class -> getBoolean(key, default as Boolean) as T?
    String::class -> getString(key, default as String?) as T?
    else -> {
        runCatching {
            val json = getString(key, null)
            json?.deserialize(type) ?: default
        }.getOrElse { default }
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
 *  给原理的key再添加多一个标识
 */
fun String.applyKey(key: String?): String {
    if (key.isNullOrEmpty()) return this
    return this + "_" + key
}

fun String?.applyKeyNullable(key: String?): String? {
    if (key.isNullOrEmpty()) return this
    return this + "_" + key
}

