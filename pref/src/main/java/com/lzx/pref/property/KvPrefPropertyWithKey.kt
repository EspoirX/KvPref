package com.lzx.pref.property

import android.annotation.SuppressLint
import android.os.SystemClock
import com.lzx.pref.*
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

object KvPrefPropertyWithKey {

    private var transactionData: Any? = null
    private var lastUpdate: Long = 0

    fun <T : Any> getValue(
        thisRef: KvPrefModel,
        clazz: KClass<T>,
        type: Type,
        property: KProperty<*>,
        key: String,
        default: T? = null,
        keyUpperCase: Boolean
    ): T? {
        val realKey = property.name.upperKey(keyUpperCase) + "_" + key
        handlerPropertiesList(thisRef, realKey, property)
        if (!thisRef.isInTransaction) {
            return thisRef.preference.getPreference(clazz, type, realKey, default)
        }
        if (lastUpdate < thisRef.transactionStartTime) {
            transactionData =
                thisRef.preference.getPreference(clazz, type, realKey, default)
            lastUpdate = SystemClock.uptimeMillis()
        }
        @Suppress("UNCHECKED_CAST")
        return transactionData as T
    }

    @SuppressLint("CommitPrefEdits")
    fun <T : Any> setValue(
        thisRef: KvPrefModel,
        clazz: KClass<T>,
        property: KProperty<*>,
        key: String,
        value: Any,
        keyUpperCase: Boolean,
        synchronous: Boolean
    ) {
        val realKey = property.name.upperKey(keyUpperCase) + "_" + key
        handlerPropertiesList(thisRef, realKey, property)
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

    private fun handlerPropertiesList(
        thisRef: KvPrefModel,
        realKey: String,
        property: KProperty<*>
    ) {
        if (!thisRef.kvProperties.containsKey(realKey)) {
            thisRef.kvProperties[realKey] = object : PreferenceProperty {
                override val propertyName: String
                    get() = property.name

                override fun preferenceKey(): String = realKey
            }
        }
    }
}
