package com.lzx.pref.property

import android.annotation.SuppressLint
import android.os.SystemClock
import com.lzx.pref.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class KvDynamicPrefProperty<T : Any>(
    private val thisRef: KvPrefModel,
    private val clazz: KClass<T>,
    private val synchronous: Boolean,
    private val key: String?,
    private val keyUpperCase: Boolean,
    private val default: T?
) : DynamicKeyPref<T> {

    private lateinit var property: KProperty<*>
    private var transactionData: Any? = null
    private var lastUpdate: Long = 0

    override val propertyName: String
        get() = property.name

    override fun get(key: String): T {
        val realKey = preferenceKey() + "_" + key
        val type = object : TypeToken<T>() {}.type
        if (!thisRef.isInTransaction) {
            thisRef.preference.getPreference(clazz, type, realKey, default)
        }
        if (lastUpdate < thisRef.transactionStartTime) {
            transactionData = thisRef.preference.getPreference(clazz, type, realKey, default)
            lastUpdate = SystemClock.uptimeMillis()
        }
        @Suppress("UNCHECKED_CAST")
        return transactionData as T
    }

    @SuppressLint("CommitPrefEdits")
    override fun set(key: String, value: T) {
        val realKey = preferenceKey() + "_" + key
        thisRef.kvProperties[realKey] = this
        if (thisRef.isInTransaction) {
            transactionData = value
            lastUpdate = SystemClock.uptimeMillis()
            thisRef.editor?.setEditor(clazz, realKey, value)
        } else {
            thisRef.preference.edit()
                .setPreference(clazz, realKey, value)
                .execute(synchronous)
        }
    }

    operator fun provideDelegate(
        thisRef: KvPrefModel,
        property: KProperty<*>
    ): ReadWriteProperty<KvPrefModel, T> {
        this.property = property
        return this
    }

    override fun preferenceKey(): String {
        val result = key ?: property.name
        return result.upperKey(keyUpperCase)
    }
}