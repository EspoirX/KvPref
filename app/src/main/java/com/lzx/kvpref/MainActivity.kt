package com.lzx.kvpref

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        findViewById<View>(R.id.write).setOnClickListener {
//            SpFileConfig.saveWithKey(SpFileConfig::haha, "哈", 13123121)
            SpFileConfig.tagJson.set("1343", "我是动态key")
            Toast.makeText(this@MainActivity, "写成功", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.red).setOnClickListener {
            val obj = SpFileConfig.tagJson
            val result = obj.get("1343")
            Toast.makeText(this@MainActivity, "obj = $obj result = $result", Toast.LENGTH_SHORT).show()
        }


    }
}