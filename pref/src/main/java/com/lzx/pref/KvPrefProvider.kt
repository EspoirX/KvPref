package com.lzx.pref

import android.content.Context
import android.content.SharedPreferences

/**
 * k-v提供者
 */
interface KvPrefProvider {
    fun get(context: Context, name: String, mode: Int): SharedPreferences
}

/**
 * 默认实现
 */
class DefKvPref : KvPrefProvider {
    override fun get(context: Context, name: String, mode: Int): SharedPreferences {
        return context.getSharedPreferences(name, mode)
    }
}