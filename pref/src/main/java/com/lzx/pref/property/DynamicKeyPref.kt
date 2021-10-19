package com.lzx.pref.property

import com.lzx.pref.KvPrefModel
import com.lzx.pref.PreferenceProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface DynamicKeyPref<T : Any> : ReadWriteProperty<KvPrefModel, T>, PreferenceProperty {

    fun get(key: String): T

    fun set(key: String, value: T)

    override fun getValue(thisRef: KvPrefModel, property: KProperty<*>): T {
        return this as T
    }

    override fun setValue(thisRef: KvPrefModel, property: KProperty<*>, value: T) {
        //
    }
}