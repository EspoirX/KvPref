package com.lzx.pref.property

import android.annotation.SuppressLint
import com.lzx.pref.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * 处理动态key情况，使用 DynamicKeyPref 委托
 */
class KvDynamicPrefProperty<T : Any>(
    private val thisRef: KvPrefModel,
    private val clazz: KClass<T>,
    private val synchronous: Boolean,
    private val key: String?,
    private val keyUpperCase: Boolean,
    private val default: T?
) : AbsDynamicPrefProperty<T>() {

    override val renameKey: String?
        get() = key
    override val isKeyUpperCase: Boolean
        get() = keyUpperCase

    override fun provideDelegate(
        thisRef: KvPrefModel,
        property: KProperty<*>
    ): ReadWriteProperty<KvPrefModel, DynamicKeyPref<T>> {
        this.property = property
        return this
    }

    override fun getRef(): KvPrefModel = thisRef

    override fun getPreference(thisRef: KvPrefModel, applyKey: String): T {
        val realKey = preferenceKey() + "_" + applyKey
        val type = object : TypeToken<T>() {}.type
        return thisRef.preference.getPreference(clazz, type, realKey, default) as T
    }

    @SuppressLint("CommitPrefEdits")
    override fun setPreference(thisRef: KvPrefModel, applyKey: String, value: T) {
        val realKey = preferenceKey() + "_" + applyKey
        thisRef.kvProperties[realKey] = this
        thisRef.preference.edit()
            .setPreference(clazz, realKey, value)
            .execute(synchronous)
    }

    override fun setEditor(thisRef: KvPrefModel, applyKey: String, value: T) {
        val realKey = preferenceKey() + "_" + applyKey
        thisRef.kvProperties[realKey] = this
        thisRef.editor?.setEditor(clazz, realKey, value)
    }
}
