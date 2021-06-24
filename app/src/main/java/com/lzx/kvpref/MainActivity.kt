package com.lzx.kvpref

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.lzx.pref.KvPrefModel
import com.lzx.pref.Serializer
import java.lang.reflect.Type

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        KvPrefModel.initKvPref(this.application, object : Serializer {
            private val gson = Gson()
            override fun serializeToJson(value: Any?): String? {
                return gson.toJson(value)
            }

            override fun deserializeFromJson(json: String?, type: Type): Any? {
                return gson.fromJson(json, type);
            }
        })

        findViewById<Button>(R.id.read).setOnClickListener {
            Log.i("XIAN", "read = " + SpTest.age)
        }

        findViewById<Button>(R.id.write).setOnClickListener {
            SpTest.people = null
            Log.i("XIAN", "read = " + SpTest.people)
        }
    }
}