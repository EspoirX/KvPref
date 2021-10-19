package com.lzx.pref.property

import android.annotation.SuppressLint
import com.lzx.pref.KvPrefModel
import com.lzx.pref.getPreference
import com.lzx.pref.setPreference
import com.lzx.pref.setEditor
import com.lzx.pref.execute
import java.lang.reflect.Type
import kotlin.reflect.KClass

/**
 * 处理可空情况
 */
open class KvPrefNullableProperty<T : Any>(
    private val clazz: KClass<T>,
    private val type: Type,
    private val synchronous: Boolean,
    private val key: String?,
    private val keyUpperCase: Boolean,
    private val default: T
) : AbstractPrefProperty<T?>() {
    override val renameKey: String?
        get() = key
    override val isKeyUpperCase: Boolean
        get() = keyUpperCase

    override fun getPreference(thisRef: KvPrefModel, applyKey: String): T? {
        return thisRef.preference.getPreference(clazz, type, preferenceKey(), default)
    }

    @SuppressLint("CommitPrefEdits")
    override fun setPreference(thisRef: KvPrefModel, applyKey: String, value: T?) {
        if (value == null) {
            thisRef.preference.edit().remove(preferenceKey()).execute(synchronous)
        } else {
            thisRef.preference.edit().setPreference(clazz, preferenceKey(), value)
                .execute(synchronous)
        }
    }

    override fun setEditor(thisRef: KvPrefModel, applyKey: String, value: T?) {
        if (value == null) {
            thisRef.editor?.remove(preferenceKey())
        } else {
            thisRef.editor?.setEditor(clazz, preferenceKey(), value)
        }
    }
}
