package com.lzx.pref

import android.annotation.SuppressLint
import android.os.SystemClock
import java.lang.reflect.Type
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class StringPref(default: String, synchronous: Boolean, key: String?, keyUpperCase: Boolean) :
    KvPrefProperty<String>(
        String::class,
        String::class.java,
        synchronous,
        key,
        keyUpperCase,
        default
    )

class StringNullablePref(
    default: String,
    synchronous: Boolean,
    key: String?,
    keyUpperCase: Boolean
) : KvPrefNullableProperty<String>(
    String::class,
    String::class.java,
    synchronous,
    key,
    keyUpperCase,
    default
)

class IntPref(default: Int, synchronous: Boolean, key: String?, keyUpperCase: Boolean) :
    KvPrefProperty<Int>(Int::class, Int::class.java, synchronous, key, keyUpperCase, default)

class IntNullablePref(default: Int, synchronous: Boolean, key: String?, keyUpperCase: Boolean) :
    KvPrefNullableProperty<Int>(
        Int::class,
        Int::class.java,
        synchronous,
        key,
        keyUpperCase,
        default
    )

class BooleanPref(default: Boolean, synchronous: Boolean, key: String?, keyUpperCase: Boolean) :
    KvPrefProperty<Boolean>(
        Boolean::class,
        Boolean::class.java,
        synchronous,
        key,
        keyUpperCase,
        default
    )

class BooleanNullablePref(
    default: Boolean,
    synchronous: Boolean,
    key: String?,
    keyUpperCase: Boolean
) :
    KvPrefNullableProperty<Boolean>(
        Boolean::class,
        Boolean::class.java,
        synchronous,
        key,
        keyUpperCase,
        default
    )

class FloatPref(default: Float, synchronous: Boolean, key: String?, keyUpperCase: Boolean) :
    KvPrefProperty<Float>(Float::class, Float::class.java, synchronous, key, keyUpperCase, default)

class FloatNullablePref(default: Float, synchronous: Boolean, key: String?, keyUpperCase: Boolean) :
    KvPrefNullableProperty<Float>(
        Float::class,
        Float::class.java,
        synchronous,
        key,
        keyUpperCase,
        default
    )

class LongPref(default: Long, synchronous: Boolean, key: String?, keyUpperCase: Boolean) :
    KvPrefProperty<Long>(Long::class, Long::class.java, synchronous, key, keyUpperCase, default)

class LongNullablePref(default: Long, synchronous: Boolean, key: String?, keyUpperCase: Boolean) :
    KvPrefNullableProperty<Long>(
        Long::class,
        Long::class.java,
        synchronous,
        key,
        keyUpperCase,
        default
    )


open class KvPrefProperty<T : Any>(
    private val clazz: KClass<T>,
    private val type: Type,
    private val synchronous: Boolean,
    private val key: String?,
    private val keyUpperCase: Boolean,
    private val default: T?
) : ReadWriteProperty<KvPrefModel, T>, PreferenceProperty {

    private var transactionData: Any? = null
    private var lastUpdate: Long = 0
    private lateinit var property: KProperty<*>

    override val propertyName: String
        get() = property.name

    override fun preferenceKey(): String {
        val result = key ?: property.name
        return if (keyUpperCase) result.toUpperCase(Locale.getDefault()) else result
    }

    operator fun provideDelegate(
        thisRef: KvPrefModel,
        property: KProperty<*>
    ): ReadWriteProperty<KvPrefModel, T> {
        this.property = property
        thisRef.kvProperties[property.name] = this
        return this
    }

    override fun getValue(thisRef: KvPrefModel, property: KProperty<*>): T {
        if (!thisRef.isInTransaction) {
            thisRef.preference.getPreference(clazz, type, preferenceKey(), default)
        }
        if (lastUpdate < thisRef.transactionStartTime) {
            transactionData =
                thisRef.preference.getPreference(clazz, type, preferenceKey(), default)
            lastUpdate = SystemClock.uptimeMillis()
        }
        @Suppress("UNCHECKED_CAST")
        return transactionData as T
    }


    @SuppressLint("CommitPrefEdits")
    override fun setValue(thisRef: KvPrefModel, property: KProperty<*>, value: T) {
        if (thisRef.isInTransaction) {
            transactionData = value
            lastUpdate = SystemClock.uptimeMillis()
            thisRef.editor?.setEditor(clazz, preferenceKey(), value)
        } else {
            thisRef.preference.edit()
                .setPreference(clazz, preferenceKey(), value)
                .execute(synchronous)
        }
    }
}

open class KvPrefNullableProperty<T : Any>(
    private val clazz: KClass<T>,
    private val type: Type,
    private val synchronous: Boolean,
    private val key: String?,
    private val keyUpperCase: Boolean,
    private val default: T
) : ReadWriteProperty<KvPrefModel, T?>, PreferenceProperty {

    private var transactionData: Any? = null
    private var lastUpdate: Long = 0
    private lateinit var property: KProperty<*>

    override val propertyName: String
        get() = property.name

    override fun preferenceKey(): String {
        val result = key ?: property.name
        return if (keyUpperCase) result.toUpperCase(Locale.getDefault()) else result
    }

    operator fun provideDelegate(
        thisRef: KvPrefModel,
        property: KProperty<*>
    ): ReadWriteProperty<KvPrefModel, T?> {
        this.property = property
        thisRef.kvProperties[property.name] = this
        return this
    }

    override fun getValue(thisRef: KvPrefModel, property: KProperty<*>): T? {
        if (!thisRef.isInTransaction) {
            thisRef.preference.getPreference(clazz, type, preferenceKey(), default)
        }
        if (lastUpdate < thisRef.transactionStartTime) {
            transactionData =
                thisRef.preference.getPreference(clazz, type, preferenceKey(), default)
            lastUpdate = SystemClock.uptimeMillis()
        }
        @Suppress("UNCHECKED_CAST")
        return transactionData as T?
    }


    @SuppressLint("CommitPrefEdits")
    override fun setValue(thisRef: KvPrefModel, property: KProperty<*>, value: T?) {
        if (thisRef.isInTransaction) {
            transactionData = value
            lastUpdate = SystemClock.uptimeMillis()
            if (value == null) {
                thisRef.editor?.remove(preferenceKey())
            } else {
                thisRef.editor?.setEditor(clazz, preferenceKey(), value)
            }
        } else {
            if (value == null) {
                thisRef.preference.edit()
                    .remove(preferenceKey())
                    .execute(synchronous)
            } else {
                thisRef.preference.edit()
                    .setPreference(clazz, preferenceKey(), value)
                    .execute(synchronous)
            }
        }
    }
}

interface PreferenceProperty {
    val propertyName: String
    fun preferenceKey(): String
}

