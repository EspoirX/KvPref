package com.lzx.pref

import com.lzx.pref.property.KvPrefNullableProperty
import com.lzx.pref.property.KvPrefProperty

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
) : KvPrefNullableProperty<Boolean>(
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


interface PreferenceProperty {
    val propertyName: String
    fun preferenceKey(): String
}

