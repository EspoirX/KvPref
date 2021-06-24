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


        findViewById<Button>(R.id.write).setOnClickListener {
            SpFileDemo.name = "大妈蛋"
            Log.i("XIAN", "read = " + SpFileDemo.name)
        }
    }
}