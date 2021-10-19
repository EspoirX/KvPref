package com.lzx.pref.property

import android.annotation.SuppressLint
import com.lzx.pref.KvPrefModel
import com.lzx.pref.TypeToken
import com.lzx.pref.getPreference
import com.lzx.pref.setPreference
import com.lzx.pref.setEditor
import com.lzx.pref.execute
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
) : AbstractPrefProperty<T>() {

    override val renameKey: String?
        get() = key
    override val isKeyUpperCase: Boolean
        get() = keyUpperCase

    override fun provideDelegate(
        thisRef: KvPrefModel,
        property: KProperty<*>
    ): ReadWriteProperty<KvPrefModel, T> {
        this.property = property
        return this
    }

    override fun getValue(thisRef: KvPrefModel, property: KProperty<*>): T {
        return this as T
    }

    override fun setValue(thisRef: KvPrefModel, property: KProperty<*>, value: T) {
        //do nothing
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
