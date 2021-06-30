package com.lzx.kvpref

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.lzx.pref.applyBulk
import com.lzx.pref.getWithKey
import com.lzx.pref.saveWithKey

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.write).setOnClickListener {


            SpFileDemo.saveWithKey(SpFileDemo::haha, "哈", 13123121)
            val result = SpFileDemo.getWithKey<Int>(SpFileDemo::haha, "哈")

            SpFileDemo.applyBulk {
                saveWithKey(SpFileDemo::haha, "13", "打卡时打开")
                saveWithKey(SpFileDemo::haha1, "24", "dasjd")
                saveWithKey(SpFileDemo::haha2, "13", "萨达安卡")
                name = "达拉斯多久啊离开"
            }

            Log.i("MainActivity", "read = " + result)
        }


    }
}