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
            SpFileConfig.tagJson.set("1343", "我是动态key")
            SpFileConfig.color.set("11111", 10000)
            SpFileConfig.name = "dsadasd"
            Toast.makeText(this@MainActivity, "写成功", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.red).setOnClickListener {
            val result = SpFileConfig.tagJson.get("1343")
            val color = SpFileConfig.color.get("11111")
            Toast.makeText(this@MainActivity, "result = $color", Toast.LENGTH_SHORT)
                .show()
        }


    }
}