package com.lzx.pref.property

import android.os.SystemClock
import com.lzx.pref.KvPrefModel
import com.lzx.pref.upperKey
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class AbsDynamicPrefProperty<T : Any?> :
    ReadWriteProperty<KvPrefModel, DynamicKeyPref<T>>, PreferenceProperty, DynamicKeyPref<T> {

    private var transactionData: Any? = null
    private var lastUpdate: Long = 0
    lateinit var property: KProperty<*>

    override val propertyName: String
        get() = property.name

    override fun preferenceKey(): String {
        val result = renameKey ?: property.name
        return result.upperKey(isKeyUpperCase)
    }

    open operator fun provideDelegate(
        thisRef: KvPrefModel,
        property: KProperty<*>
    ): ReadWriteProperty<KvPrefModel, DynamicKeyPref<T>> {
        this.property = property
        thisRef.kvProperties[property.name] = this
        return this
    }

    override operator fun getValue(thisRef: KvPrefModel, property: KProperty<*>): DynamicKeyPref<T> {
        return this
    }

    override fun setValue(thisRef: KvPrefModel, property: KProperty<*>, value: DynamicKeyPref<T>) {
        //
    }

    override fun get(key: String): T {
        if (!getRef().isInTransaction) {
            return getPreference(getRef(), key)
        }
        if (lastUpdate < getRef().transactionStartTime) {
            transactionData = getPreference(getRef(), key)
            lastUpdate = SystemClock.uptimeMillis()
        }
        @Suppress("UNCHECKED_CAST")
        return transactionData as T
    }

    override fun set(key: String, value: T) {
        val realKey = preferenceKey() + "_" + key
        getRef().kvProperties[realKey] = this
        if (getRef().isInTransaction) {
            transactionData = value
            lastUpdate = SystemClock.uptimeMillis()
            setEditor(getRef(), key, value)
        } else {
            setPreference(getRef(), key, value)
        }
    }

    open fun getRef(): KvPrefModel {
        throw IllegalStateException("only DynamicKey must override this")
    }

    abstract val renameKey: String?
    abstract val isKeyUpperCase: Boolean
    abstract fun getPreference(thisRef: KvPrefModel, applyKey: String): T
    abstract fun setPreference(thisRef: KvPrefModel, applyKey: String, value: T)
    abstract fun setEditor(thisRef: KvPrefModel, applyKey: String, value: T)
}
