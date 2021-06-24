package com.lzx.kvpref

import android.app.Application
import com.google.gson.Gson
import com.lzx.pref.KvPrefModel
import com.lzx.pref.Serializer
import java.lang.reflect.Type

class DemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        KvPrefModel.initKvPref(this, object : Serializer {
            private val gson = Gson()
            override fun serializeToJson(value: Any?): String? {
                return gson.toJson(value)
            }

            override fun deserializeFromJson(json: String?, type: Type): Any? {
                return gson.fromJson(json, type);
            }
        })
    }
}