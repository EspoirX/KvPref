package com.lzx.pref.property

import android.annotation.SuppressLint
import android.os.SystemClock
import com.lzx.pref.KvPrefModel
import com.lzx.pref.PreferenceProperty
import com.lzx.pref.getPreference
import com.lzx.pref.execute
import com.lzx.pref.setEditor
import com.lzx.pref.setPreference
import java.lang.reflect.Type
import java.util.Locale
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

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