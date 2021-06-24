package com.lzx.kvpref

import com.lzx.pref.KvPrefModel

object SpTest : KvPrefModel("贤哥") {
    var people: People? by objPrefNullable( People().apply { age = 100;name = "吃狗屎" })

    var age: String? by stringPrefNullable()
}

class People {
    var age: Int = 0
    var name: String? = null

    override fun toString(): String {
        return "People(age=$age, name=$name)"
    }
}