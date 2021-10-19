package com.lzx.kvpref.impl

import android.content.Context
import android.content.SharedPreferences
import com.lzx.pref.KvPrefProvider
import com.tencent.mmkv.MMKV

class MmKvPref : KvPrefProvider {
    override fun get(context: Context, name: String, mode: Int): SharedPreferences {
        if (MMKV.getRootDir().isNullOrEmpty()) {
            MMKV.initialize(context)
        }
        return MMKV.mmkvWithID(name, MMKV.SINGLE_PROCESS_MODE) as SharedPreferences
    }
}