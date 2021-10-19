package com.lzx.pref.property

import android.annotation.SuppressLint
import android.os.SystemClock
import com.lzx.pref.*
import java.lang.reflect.Type
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

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
